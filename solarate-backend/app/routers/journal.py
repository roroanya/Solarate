import asyncio

from fastapi import APIRouter, HTTPException

from .. import curated, mappers
from .. import schemas as s

router = APIRouter(prefix="/journal", tags=["Journal"])


def _covers(page: dict) -> list[str]:
    covers = [
        mappers.img(m["backdrop_path"], "w780")
        for m in page.get("results", [])
        if m.get("backdrop_path")
    ]
    return covers[:3]


@router.get("", response_model=list[s.JournalSummary], summary="Tab Journal: daftar artikel")
async def journal_list():
    pages = await asyncio.gather(
        *(curated.fetch_list_page(a["list_slug"], 1) for a in curated.JOURNAL)
    )
    return [
        s.JournalSummary(
            slug=a["slug"], title=a["title"], subtitle=a["subtitle"], cover_urls=_covers(p)
        )
        for a, p in zip(curated.JOURNAL, pages)
    ]


@router.get("/{slug}", response_model=s.JournalDetail, summary="Isi artikel")
async def journal_detail(slug: str):
    article = next((a for a in curated.JOURNAL if a["slug"] == slug), None)
    if article is None:
        raise HTTPException(404, "Artikel tidak ditemukan")
    page = await curated.fetch_list_page(article["list_slug"], 1)
    films = [
        mappers.movie_summary(m) for m in page.get("results", []) if m.get("poster_path")
    ][:10]
    return s.JournalDetail(
        slug=article["slug"],
        title=article["title"],
        subtitle=article["subtitle"],
        cover_urls=_covers(page),
        paragraphs=article["paragraphs"],
        list_slug=article["list_slug"],
        films=films,
    )
