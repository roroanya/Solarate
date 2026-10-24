from fastapi import APIRouter

from .. import schemas as s
from ..tmdb import tmdb

router = APIRouter(prefix="/meta", tags=["Meta (filter Browse)"])
DAY = 86400


@router.get("/genres", response_model=list[s.Genre])
async def genres():
    data = await tmdb.get("/genre/movie/list", ttl=DAY)
    return [s.Genre(id=g["id"], name=g["name"]) for g in data.get("genres", [])]


@router.get("/countries", response_model=list[s.Country])
async def countries():
    data = await tmdb.get("/configuration/countries", ttl=DAY)
    items = [s.Country(code=c["iso_3166_1"], name=c["english_name"]) for c in data]
    return sorted(items, key=lambda c: c.name)


@router.get("/languages", response_model=list[s.Language])
async def languages():
    data = await tmdb.get("/configuration/languages", ttl=DAY)
    items = [
        s.Language(code=l["iso_639_1"], name=l.get("english_name") or l.get("name") or l["iso_639_1"])
        for l in data
        if l.get("iso_639_1") and l.get("iso_639_1") != "xx"
    ]
    return sorted(items, key=lambda l: l.name)


@router.get("/sort-options", response_model=list[s.SortOption], summary="Opsi sort untuk /movies/discover")
async def sort_options():
    return [
        s.SortOption(key="popularity.desc", label="Popularity"),
        s.SortOption(key="vote_average.desc", label="Highest rated"),
        s.SortOption(key="primary_release_date.desc", label="Newest first"),
        s.SortOption(key="primary_release_date.asc", label="Earliest first"),
        s.SortOption(key="revenue.desc", label="Highest grossing"),
    ]
