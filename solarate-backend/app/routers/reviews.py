import asyncio

from fastapi import APIRouter, Query

from .. import mappers
from .. import schemas as s
from ..tmdb import tmdb

router = APIRouter(tags=["Reviews"])


@router.get("/reviews/popular", response_model=list[s.Review], summary="Tab Reviews: popular this week")
async def popular_reviews(limit: int = Query(20, ge=1, le=50)):
    trending = await tmdb.get("/trending/movie/week")
    movies = [m for m in trending.get("results", []) if m.get("poster_path")][:15]
    responses = await asyncio.gather(
        *(tmdb.get(f"/movie/{m['id']}/reviews") for m in movies), return_exceptions=True
    )
    feed: list[s.Review] = []
    for movie, res in zip(movies, responses):
        if isinstance(res, Exception):
            continue
        summary = mappers.movie_summary(movie)
        for r in res.get("results", [])[:2]:
            feed.append(mappers.review(r, summary))
    return feed[:limit]
