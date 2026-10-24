import datetime as dt
from typing import Literal

from fastapi import APIRouter, Query

from .. import mappers
from .. import schemas as s
from ..config import settings
from ..tmdb import tmdb

router = APIRouter(prefix="/movies", tags=["Movies"])

SortBy = Literal[
    "popularity.desc",
    "vote_average.desc",
    "primary_release_date.desc",
    "primary_release_date.asc",
    "revenue.desc",
]
Pg = Query(1, ge=1, le=500, description="Nomor halaman")


async def _feed(path: str, page: int, params: dict | None = None) -> s.Page[s.MovieSummary]:
    data = await tmdb.get(path, {"page": page, **(params or {})})
    return mappers.page_of(data, mappers.movie_summary, require_poster=True)


@router.get("/trending", response_model=s.Page[s.MovieSummary], summary="Popular today / this week")
async def trending(
    page: int = Pg,
    window: Literal["day", "week"] = Query("week", description="'day' = Popular today, 'week' = Popular this week"),
):
    return await _feed(f"/trending/movie/{window}", page)


@router.get("/popular", response_model=s.Page[s.MovieSummary], summary="Most popular")
async def popular(page: int = Pg):
    return await _feed("/movie/popular", page)


@router.get("/top-rated", response_model=s.Page[s.MovieSummary], summary="Highest rated")
async def top_rated(page: int = Pg):
    return await _feed("/movie/top_rated", page)


@router.get("/now-playing", response_model=s.Page[s.MovieSummary], summary="Sedang tayang")
async def now_playing(page: int = Pg, region: str = Query(None, min_length=2, max_length=2)):
    return await _feed("/movie/now_playing", page, {"region": region or settings.default_region})


@router.get("/upcoming", response_model=s.Page[s.MovieSummary], summary="Most anticipated")
async def upcoming(page: int = Pg):
    return await _feed(
        "/discover/movie",
        page,
        {
            "include_adult": "false",
            "sort_by": "popularity.desc",
            "primary_release_date.gte": dt.date.today().isoformat(),
        },
    )


@router.get("/discover", response_model=s.Page[s.MovieSummary], summary="Browse by release date / genre / country / language",
)
async def discover(
    page: int = Pg,
    genre: int | None = Query(None, description="ID genre, lihat /api/meta/genres"),
    year: int | None = Query(None, ge=1870, le=2100),
    decade: int | None = Query(None, ge=1870, le=2100, description="Tahun awal dekade, mis. 1990"),
    country: str | None = Query(None, min_length=2, max_length=2, description="ISO 3166-1, mis. ID"),
    language: str | None = Query(None, min_length=2, max_length=3, description="ISO 639-1, mis. ko"),
    sort_by: SortBy = "popularity.desc",
    min_votes: int | None = Query(None, ge=0, description="Minimal jumlah vote"),
):
    params: dict = {"include_adult": "false", "sort_by": sort_by}
    if genre:
        params["with_genres"] = genre
    if year:
        params["primary_release_year"] = year
    elif decade:
        params["primary_release_date.gte"] = f"{decade}-01-01"
        params["primary_release_date.lte"] = f"{decade + 9}-12-31"
    if country:
        params["with_origin_country"] = country.upper()
    if language:
        params["with_original_language"] = language.lower()
    if min_votes is not None:
        params["vote_count.gte"] = min_votes
    elif sort_by == "vote_average.desc":
        params["vote_count.gte"] = 300  # hindari film dengan 1-2 vote mendominasi
    return await _feed("/discover/movie", page, params)


@router.get(
    "/{movie_id}",
    response_model=s.MovieDetail,
    summary="Detail film lengkap",
)
async def movie_detail(
    movie_id: int,
    region: str = Query(
        None,
        min_length=2,
        max_length=2,
        description="Negara untuk 'Where to watch'",
    ),
):
    region = (region or settings.default_region).upper()

    data = await tmdb.get(
        f"/movie/{movie_id}",
        {
            "append_to_response": (
                "credits,videos,similar,recommendations,reviews,"
                "watch/providers,release_dates"
            ),
            "include_video_language": "en,null",
        },
        ttl=1800,
    )

    return mappers.movie_detail(data, region)


@router.get("/{movie_id}/similar", response_model=s.Page[s.MovieSummary])
async def similar(movie_id: int, page: int = Pg):
    return await _feed(f"/movie/{movie_id}/similar", page)


@router.get("/{movie_id}/recommendations", response_model=s.Page[s.MovieSummary])
async def recommendations(movie_id: int, page: int = Pg):
    return await _feed(f"/movie/{movie_id}/recommendations", page)


@router.get("/{movie_id}/reviews", response_model=s.Page[s.Review], summary="Semua review sebuah film")
async def movie_reviews(movie_id: int, page: int = Pg):
    data = await tmdb.get(f"/movie/{movie_id}/reviews", {"page": page})
    return mappers.page_of(data, mappers.review)
