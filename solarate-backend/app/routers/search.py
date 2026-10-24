import asyncio
from typing import Literal

from fastapi import APIRouter, Query

from .. import mappers
from .. import schemas as s
from ..tmdb import tmdb

router = APIRouter(tags=["Search"])


@router.get("/search", response_model=s.SearchResponse, summary="Cari film dan/atau orang")
async def search(
    q: str = Query(..., min_length=1, max_length=100),
    type: Literal["all", "movie", "person"] = "all",
    page: int = Query(1, ge=1, le=500),
):
    params = {"query": q, "page": page, "include_adult": "false"}
    movies = people = None

    if type == "all":
        m, p = await asyncio.gather(
            tmdb.get("/search/movie", params, ttl=300),
            tmdb.get("/search/person", params, ttl=300),
        )
        movies = mappers.page_of(m, mappers.movie_summary)
        people = mappers.page_of(p, mappers.person_summary)
    elif type == "movie":
        movies = mappers.page_of(await tmdb.get("/search/movie", params, ttl=300), mappers.movie_summary)
    else:
        people = mappers.page_of(await tmdb.get("/search/person", params, ttl=300), mappers.person_summary)

    return s.SearchResponse(query=q, movies=movies, people=people)
