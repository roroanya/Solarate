import asyncio

from fastapi import APIRouter, HTTPException, Query

from .. import curated, mappers
from .. import schemas as s

router = APIRouter(prefix="/lists", tags=["Lists"])


@router.get("", response_model=list[s.ListSummary], summary="Tab Lists: daftar populer")
async def all_lists(official: bool | None = Query(None, description="true = Official lists, false = Featured lists")):
    slugs = [
        slug for slug, d in curated.LISTS.items() if official is None or d["official"] == official
    ]
    pages = await asyncio.gather(*(curated.fetch_list_page(slug, 1) for slug in slugs))
    return [curated.list_summary(slug, page) for slug, page in zip(slugs, pages)]


@router.get("/{slug}", response_model=s.ListDetail, summary="Isi sebuah list")
async def list_detail(slug: str, page: int = Query(1, ge=1, le=25)):
    if slug not in curated.LISTS:
        raise HTTPException(404, "List tidak ditemukan")
    data = await curated.fetch_list_page(slug, page)
    defn = curated.LISTS[slug]
    return s.ListDetail(
        slug=slug,
        title=defn["title"],
        description=defn["description"],
        author=defn["author"],
        official=defn["official"],
        film_count=data.get("total_results", 0),
        films=mappers.page_of(data, mappers.movie_summary, require_poster=True),
    )
