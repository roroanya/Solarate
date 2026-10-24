import logging
from contextlib import asynccontextmanager

import httpx
from fastapi import APIRouter, FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from .config import settings
from .routers import images, journal, lists, meta, movies, people, reviews, search
from .tmdb import TMDBError, tmdb

log = logging.getLogger("solarate")


@asynccontextmanager
async def lifespan(app: FastAPI):
    if not settings.tmdb_read_token and not settings.tmdb_api_key:
        log.warning("TMDB_READ_TOKEN / TMDB_API_KEY belum diisi di .env")
    await tmdb.start()
    app.state.img_client = httpx.AsyncClient(timeout=15)
    yield
    await tmdb.close()
    await app.state.img_client.aclose()


app = FastAPI(
    title="Solarate Movie API",
    description="Backend film untuk aplikasi Solarate (clone Letterboxd). Data dari TMDB, tanpa database.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_methods=["GET"], allow_headers=["*"]
)


@app.exception_handler(TMDBError)
async def tmdb_error_handler(_, exc: TMDBError):
    return JSONResponse(status_code=exc.status_code, content={"detail": exc.message})


api = APIRouter(prefix="/api")
for r in (movies, search, people, meta, reviews, lists, journal):
    api.include_router(r.router)
app.include_router(api)
app.include_router(images.router)


@app.get("/", tags=["System"])
async def root():
    return {"app": "Solarate Movie API", "docs": "/docs", "health": "/health"}


@app.get("/health", tags=["System"])
async def health():
    return {"status": "ok"}


@app.get("/health/tmdb", tags=["System"], summary="Cek koneksi & token TMDB")
async def health_tmdb():
    await tmdb.get("/configuration", ttl=0)
    return {"status": "ok", "tmdb": "reachable"}
