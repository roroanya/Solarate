import re

from fastapi import APIRouter, HTTPException, Request, Response

from ..config import settings

router = APIRouter(tags=["Image proxy"])

ALLOWED_SIZES = {"w92", "w154", "w185", "w300", "w342", "w500", "w780", "w1280", "h632", "original"}
FILENAME = re.compile(r"^[A-Za-z0-9_\-]+\.(jpg|jpeg|png)$")


@router.get(
    "/img/{size}/{filename}",
    summary="Proxy gambar TMDB (berguna kalau image.tmdb.org diblokir ISP)",
    include_in_schema=True,
)
async def image_proxy(size: str, filename: str, request: Request):
    if size not in ALLOWED_SIZES or not FILENAME.match(filename):
        raise HTTPException(400, "Parameter gambar tidak valid")
    url = f"{settings.tmdb_image_origin}/t/p/{size}/{filename}"
    try:
        resp = await request.app.state.img_client.get(url)
    except Exception:
        raise HTTPException(502, "Gagal mengambil gambar dari TMDB")
    if resp.status_code != 200:
        raise HTTPException(resp.status_code if resp.status_code in (404, 403) else 502, "Gambar tidak tersedia")
    return Response(
        content=resp.content,
        media_type=resp.headers.get("content-type", "image/jpeg"),
        headers={"Cache-Control": "public, max-age=604800"},
    )
