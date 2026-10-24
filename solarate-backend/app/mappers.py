"""Ubah respons mentah TMDB jadi bentuk JSON yang ramah untuk aplikasi Android."""
import re
from typing import Any, Callable

from . import schemas as s
from .config import settings

RELEASE_TYPES = {
    1: "Premiere",
    2: "Theatrical (limited)",
    3: "Theatrical",
    4: "Digital",
    5: "Physical",
    6: "TV",
}

CREW_ROLES = {
    "Director": "Director",
    "Writer": "Writer",
    "Screenplay": "Writer",
    "Story": "Writer",
    "Producer": "Producer",
    "Director of Photography": "Cinematography",
    "Original Music Composer": "Composer",
    "Editor": "Editor",
    "Casting": "Casting",
}


def img(path: str | None, size: str = "w500") -> str | None:
    if not path:
        return None
    if path.startswith("http"):
        return path
    return f"{settings.image_base_url}/{size}{path}"


def _year(date: str | None) -> int | None:
    if date and len(date) >= 4 and date[:4].isdigit():
        return int(date[:4])
    return None


def rating5(avg: float | None, votes: int | None) -> float | None:
    if not votes or avg is None:
        return None
    return round(avg / 2, 1)


def movie_summary(d: dict[str, Any]) -> s.MovieSummary:
    return s.MovieSummary(
        id=d["id"],
        title=d.get("title") or d.get("name") or "",
        year=_year(d.get("release_date")),
        release_date=d.get("release_date") or None,
        poster_url=img(d.get("poster_path"), "w342"),
        backdrop_url=img(d.get("backdrop_path"), "w780"),
        rating=rating5(d.get("vote_average"), d.get("vote_count")),
        vote_count=d.get("vote_count") or 0,
    )


def person_summary(d: dict[str, Any]) -> s.PersonSummary:
    return s.PersonSummary(
        id=d["id"],
        name=d.get("name", ""),
        profile_url=img(d.get("profile_path"), "w185"),
        known_for_department=d.get("known_for_department"),
    )


def page_of(
    data: dict[str, Any],
    mapper: Callable[[dict[str, Any]], Any],
    require_poster: bool = False,
) -> s.Page:
    items = data.get("results", [])
    if require_poster:
        items = [i for i in items if i.get("poster_path")]
    return s.Page(
        page=data.get("page", 1),
        total_pages=min(data.get("total_pages", 1), 500),
        total_results=data.get("total_results", len(items)),
        results=[mapper(i) for i in items],
    )


def _excerpt(text: str, limit: int = 280) -> str:
    flat = re.sub(r"\s+", " ", text).strip()
    if len(flat) <= limit:
        return flat
    return flat[:limit].rsplit(" ", 1)[0] + "…"


def _avatar(path: str | None) -> str | None:
    if not path:
        return None
    if path.startswith("/http"):  # TMDB kadang menyimpan URL gravatar dengan prefix "/"
        return path[1:]
    return img(path, "w185")


def review(d: dict[str, Any], movie: s.MovieSummary | None = None) -> s.Review:
    details = d.get("author_details") or {}
    raw = details.get("rating")
    stars = round(raw) / 2 if raw is not None else None  # 0-10 -> 0-5 kelipatan 0.5
    content = d.get("content", "")
    return s.Review(
        id=str(d.get("id", "")),
        author=details.get("name") or d.get("author") or "Anonymous",
        username=details.get("username") or d.get("author"),
        avatar_url=_avatar(details.get("avatar_path")),
        rating=stars,
        content=content,
        excerpt=_excerpt(content),
        created_at=d.get("created_at"),
        url=d.get("url"),
        movie=movie,
    )


def _provider_list(items: list[dict[str, Any]] | None) -> list[s.Provider]:
    return [
        s.Provider(
            id=p["provider_id"],
            name=p.get("provider_name", ""),
            logo_url=img(p.get("logo_path"), "w92"),
        )
        for p in (items or [])
    ]


def _providers(data: dict[str, Any] | None, region: str) -> s.Providers:
    results = ((data or {}).get("results")) or {}
    for code in (region, "US"):
        entry = results.get(code)
        if entry:
            return s.Providers(
                region=code,
                link=entry.get("link"),
                flatrate=_provider_list(entry.get("flatrate")),
                rent=_provider_list(entry.get("rent")),
                buy=_provider_list(entry.get("buy")),
            )
    return s.Providers()


def _releases(data: dict[str, Any] | None, region: str) -> tuple[list[s.Release], str | None]:
    out: list[s.Release] = []
    certification: str | None = None
    fallback: str | None = None
    for country in (data or {}).get("results", []):
        code = country.get("iso_3166_1", "")
        for r in country.get("release_dates", []):
            cert = r.get("certification") or None
            out.append(
                s.Release(
                    country=code,
                    type=RELEASE_TYPES.get(r.get("type"), "Other"),
                    date=(r.get("release_date") or "")[:10] or None,
                    certification=cert,
                )
            )
            if cert and code == region and not certification:
                certification = cert
            if cert and code == "US" and not fallback:
                fallback = cert
    out.sort(key=lambda r: r.date or "9999")
    return out[:40], certification or fallback


def _trailer(videos: dict[str, Any] | None) -> str | None:
    vids = [
        v
        for v in (videos or {}).get("results", [])
        if v.get("site") == "YouTube" and v.get("type") in ("Trailer", "Teaser")
    ]
    vids.sort(key=lambda v: (v.get("type") != "Trailer", not v.get("official", False)))
    return vids[0].get("key") if vids else None


def movie_detail(d: dict[str, Any], region: str) -> s.MovieDetail:
    credits = d.get("credits") or {}
    cast = [
        s.CastMember(
            id=c["id"],
            name=c.get("name", ""),
            character=c.get("character") or None,
            profile_url=img(c.get("profile_path"), "w185"),
        )
        for c in credits.get("cast", [])[:20]
    ]

    crew: list[s.CrewMember] = []
    seen: set[tuple[int, str]] = set()
    for c in credits.get("crew", []):
        role = CREW_ROLES.get(c.get("job", ""))
        if not role or (c["id"], role) in seen:
            continue
        seen.add((c["id"], role))
        crew.append(
            s.CrewMember(
                id=c["id"],
                name=c.get("name", ""),
                role=role,
                profile_url=img(c.get("profile_path"), "w185"),
            )
        )
    directors = [c.name for c in crew if c.role == "Director"]

    releases, certification = _releases(d.get("release_dates"), region)
    key = _trailer(d.get("videos"))

    return s.MovieDetail(
        id=d["id"],
        title=d.get("title", ""),
        original_title=d.get("original_title"),
        tagline=d.get("tagline") or None,
        overview=d.get("overview") or None,
        year=_year(d.get("release_date")),
        release_date=d.get("release_date") or None,
        runtime=d.get("runtime") or None,
        status=d.get("status"),
        genres=[s.Genre(id=g["id"], name=g["name"]) for g in d.get("genres", [])],
        poster_url=img(d.get("poster_path"), "w500"),
        backdrop_url=img(d.get("backdrop_path"), "w1280"),
        rating=rating5(d.get("vote_average"), d.get("vote_count")),
        vote_count=d.get("vote_count") or 0,
        directors=directors,
        cast=cast,
        crew=crew,
        trailer_url=f"https://www.youtube.com/watch?v={key}" if key else None,
        trailer_youtube_key=key,
        studios=[c.get("name", "") for c in d.get("production_companies", [])],
        countries=[c.get("name", "") for c in d.get("production_countries", [])],
        languages=[l.get("english_name", "") for l in d.get("spoken_languages", [])],
        original_language=d.get("original_language"),
        certification=certification,
        providers=_providers(d.get("watch/providers"), region),
        releases=releases,
        similar=[movie_summary(m) for m in (d.get("similar") or {}).get("results", [])[:12]],
        recommendations=[
            movie_summary(m) for m in (d.get("recommendations") or {}).get("results", [])[:12]
        ],
        reviews=[review(r) for r in (d.get("reviews") or {}).get("results", [])[:5]],
    )
