"""Smoke test offline: TMDB di-mock, jadi tidak butuh internet/token."""
import re

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.tmdb import TMDBError, tmdb


def mv(i, **kw):
    return {
        "id": i, "title": f"Movie {i}", "release_date": "2024-05-01",
        "poster_path": f"/p{i}.jpg", "backdrop_path": f"/b{i}.jpg",
        "vote_average": 8.0, "vote_count": 1000, "popularity": 100 - i, **kw,
    }


def paged(items, total=57):
    return {"page": 1, "total_pages": 3, "total_results": total, "results": items}


REVIEW = {
    "id": "r1", "author": "bob", "content": "Great film. " * 50, "created_at": "2024-01-01T00:00:00Z",
    "url": "https://x", "author_details": {"name": "Bob", "username": "bob", "avatar_path": "/https://gravatar/x.jpg", "rating": 9},
}

DETAIL = {
    **mv(10, runtime=120, tagline="t", overview="o", status="Released", original_language="en"),
    "genres": [{"id": 28, "name": "Action"}],
    "production_companies": [{"name": "Studio"}],
    "production_countries": [{"name": "Indonesia"}],
    "spoken_languages": [{"english_name": "English"}],
    "credits": {
        "cast": [{"id": 1, "name": "Actor", "character": "Hero", "profile_path": "/a.jpg"}],
        "crew": [{"id": 2, "name": "Dir", "job": "Director", "profile_path": None}],
    },
    "videos": {"results": [{"site": "YouTube", "type": "Trailer", "key": "abc", "official": True}]},
    "similar": paged([mv(11)]),
    "recommendations": paged([mv(12)]),
    "reviews": paged([REVIEW]),
    "watch/providers": {"results": {"US": {"link": "l", "flatrate": [{"provider_id": 8, "provider_name": "Net", "logo_path": "/n.jpg"}]}}},
    "release_dates": {"results": [{"iso_3166_1": "US", "release_dates": [{"certification": "PG-13", "release_date": "2024-05-01T00:00:00.000Z", "type": 3}]}]},
}


async def fake_get(path, params=None, ttl=None):
    if path in ("/movie/popular", "/movie/top_rated", "/movie/now_playing", "/discover/movie") or path.startswith("/trending"):
        return paged([mv(i) for i in range(1, 6)] + [{"id": 99, "title": "NoPoster", "poster_path": None}])
    if path == "/search/movie":
        return paged([mv(1)])
    if path == "/search/person":
        return paged([{"id": 5, "name": "P", "profile_path": None, "known_for_department": "Acting"}])
    if re.fullmatch(r"/movie/\d+/reviews", path):
        return paged([REVIEW])
    if re.fullmatch(r"/movie/\d+/(similar|recommendations)", path):
        return paged([mv(3)])
    if path == "/movie/10":
        return DETAIL
    if path == "/person/7":
        return {"id": 7, "name": "Pers", "profile_path": "/x.jpg", "movie_credits": {"cast": [mv(1), mv(1), mv(2)], "crew": [{**mv(3), "job": "Director"}]}}
    if path == "/genre/movie/list":
        return {"genres": [{"id": 28, "name": "Action"}]}
    if path == "/configuration/countries":
        return [{"iso_3166_1": "ID", "english_name": "Indonesia"}]
    if path == "/configuration/languages":
        return [{"iso_639_1": "id", "english_name": "Indonesian"}, {"iso_639_1": "xx", "english_name": "No Language"}]
    if path == "/configuration":
        return {}
    raise TMDBError(404, "Data tidak ditemukan di TMDB")


@pytest.fixture()
def client(monkeypatch):
    monkeypatch.setattr(tmdb, "get", fake_get)
    with TestClient(app) as c:
        yield c


def test_system(client):
    assert client.get("/health").json() == {"status": "ok"}
    assert client.get("/health/tmdb").status_code == 200


@pytest.mark.parametrize("p", ["trending", "popular", "top-rated", "now-playing", "upcoming", "discover"])
def test_feeds_filter_no_poster(client, p):
    r = client.get(f"/api/movies/{p}")
    assert r.status_code == 200
    body = r.json()
    assert len(body["results"]) == 5  # item tanpa poster dibuang
    first = body["results"][0]
    assert first["rating"] == 4.0 and first["year"] == 2024
    assert first["poster_url"].endswith("/w342/p1.jpg")


def test_discover_params_and_validation(client):
    assert client.get("/api/movies/discover?genre=28&decade=1990&sort_by=vote_average.desc").status_code == 200
    assert client.get("/api/movies/discover?sort_by=hack").status_code == 422


def test_detail(client):
    d = client.get("/api/movies/10?region=ID").json()
    assert d["directors"] == ["Dir"]
    assert d["trailer_youtube_key"] == "abc"
    assert d["providers"]["region"] == "US"  # fallback dari ID
    assert d["certification"] == "PG-13"
    assert d["reviews"][0]["rating"] == 4.5
    assert d["reviews"][0]["avatar_url"] == "https://gravatar/x.jpg"
    assert d["reviews"][0]["excerpt"].endswith("…")
    assert d["similar"] and d["recommendations"]


def test_not_found_and_sub_routes(client):
    assert client.get("/api/movies/404").status_code == 404
    assert client.get("/api/movies/10/reviews").status_code == 200
    assert client.get("/api/movies/10/similar").status_code == 200


def test_search(client):
    r = client.get("/api/search?q=abc").json()
    assert r["movies"] and r["people"]
    r = client.get("/api/search?q=abc&type=movie").json()
    assert r["people"] is None
    assert client.get("/api/search").status_code == 422


def test_people_and_meta(client):
    p = client.get("/api/people/7").json()
    assert [f["id"] for f in p["films_as_actor"]] == [1, 2]  # duplikat dibuang
    assert p["films_as_director"][0]["id"] == 3
    assert client.get("/api/meta/genres").json()[0]["name"] == "Action"
    assert client.get("/api/meta/countries").json()[0]["code"] == "ID"
    assert [l["code"] for l in client.get("/api/meta/languages").json()] == ["id"]
    assert len(client.get("/api/meta/sort-options").json()) == 5


def test_reviews_feed(client):
    r = client.get("/api/reviews/popular?limit=5").json()
    assert len(r) == 5 and r[0]["movie"]["id"] == 1


def test_lists(client):
    allv = client.get("/api/lists").json()
    assert len(allv) == 11 and len(allv[0]["previews"]) == 5
    official = client.get("/api/lists?official=true").json()
    assert all(x["official"] for x in official)
    d = client.get("/api/lists/hidden-gems?page=2").json()
    assert d["films"]["results"]
    assert client.get("/api/lists/nope").status_code == 404
    assert client.get("/api/lists/hidden-gems?page=9").json()["films"]["results"] == []


def test_journal(client):
    j = client.get("/api/journal").json()
    assert len(j) == 5 and j[0]["cover_urls"]
    d = client.get(f"/api/journal/{j[0]['slug']}").json()
    assert d["paragraphs"] and d["films"]
    assert client.get("/api/journal/nope").status_code == 404


def test_image_proxy_validation(client):
    assert client.get("/img/bad/x.jpg").status_code == 400
    assert client.get("/img/w500/..%2Fetc.jpg").status_code in (400, 404)
