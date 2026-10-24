import asyncio
import logging
from typing import Any

import httpx

from .cache import TTLCache
from .config import settings

log = logging.getLogger("solarate.tmdb")


class TMDBError(Exception):
    def __init__(self, status_code: int, message: str):
        super().__init__(message)
        self.status_code = status_code
        self.message = message


class TMDBClient:
    """Client async tipis untuk TMDB v3 + cache TTL."""

    def __init__(self) -> None:
        self._client: httpx.AsyncClient | None = None
        self._cache = TTLCache(maxsize=2000)

    async def start(self) -> None:
        headers = {"accept": "application/json"}
        if settings.tmdb_read_token:
            headers["Authorization"] = f"Bearer {settings.tmdb_read_token}"
        self._client = httpx.AsyncClient(
            base_url=settings.tmdb_base_url,
            headers=headers,
            timeout=settings.request_timeout,
        )

    async def close(self) -> None:
        if self._client:
            await self._client.aclose()
            self._client = None

    async def get(
        self, path: str, params: dict[str, Any] | None = None, ttl: float | None = None
    ) -> Any:
        ttl = settings.cache_ttl_seconds if ttl is None else ttl
        query = {k: v for k, v in (params or {}).items() if v is not None}
        query.setdefault("language", settings.default_language)
        if not settings.tmdb_read_token and settings.tmdb_api_key:
            query["api_key"] = settings.tmdb_api_key

        key = (path, tuple(sorted((k, str(v)) for k, v in query.items())))
        hit = self._cache.get(key)
        if hit is not None:
            return hit

        if self._client is None:
            await self.start()

        for attempt in range(3):
            try:
                resp = await self._client.get(path, params=query)  # type: ignore[union-attr]
            except httpx.TransportError as exc:
                log.warning("TMDB transport error (%s) attempt %d", exc, attempt + 1)
                await asyncio.sleep(0.4 * (attempt + 1))
                continue

            if resp.status_code == 200:
                data = resp.json()
                self._cache.set(key, data, ttl)
                return data
            if resp.status_code == 404:
                raise TMDBError(404, "Data tidak ditemukan di TMDB")
            if resp.status_code == 401:
                raise TMDBError(502, "Token/API key TMDB tidak valid (cek file .env)")
            if resp.status_code == 429:
                raise TMDBError(503, "Terlalu banyak request ke TMDB, coba lagi sebentar")
            if resp.status_code >= 500:
                await asyncio.sleep(0.4 * (attempt + 1))
                continue
            raise TMDBError(502, f"TMDB membalas status {resp.status_code}")

        raise TMDBError(502, "Tidak bisa terhubung ke TMDB (cek internet/VPN/DNS)")


tmdb = TMDBClient()
