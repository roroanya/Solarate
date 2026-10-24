from fastapi import APIRouter

from .. import mappers
from .. import schemas as s
from ..tmdb import tmdb

router = APIRouter(prefix="/people", tags=["People"])


def _unique_films(items: list[dict], limit: int) -> list[s.MovieSummary]:
    seen: set[int] = set()
    films = []
    for m in sorted(items, key=lambda x: x.get("popularity", 0), reverse=True):
        if m["id"] in seen or not m.get("poster_path"):
            continue
        seen.add(m["id"])
        films.append(mappers.movie_summary(m))
        if len(films) >= limit:
            break
    return films


@router.get("/{person_id}", response_model=s.PersonDetail, summary="Profil aktor/sutradara")
async def person_detail(person_id: int):
    d = await tmdb.get(f"/person/{person_id}", {"append_to_response": "movie_credits"}, ttl=1800)
    credits = d.get("movie_credits") or {}
    directed = [c for c in credits.get("crew", []) if c.get("job") == "Director"]
    base = mappers.person_summary(d)
    return s.PersonDetail(
        **base.model_dump(),
        biography=d.get("biography") or None,
        birthday=d.get("birthday"),
        deathday=d.get("deathday"),
        place_of_birth=d.get("place_of_birth"),
        films_as_actor=_unique_films(credits.get("cast", []), 40),
        films_as_director=_unique_films(directed, 20),
    )
