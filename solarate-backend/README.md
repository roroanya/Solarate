# Solarate Movie API (Backend)

Backend untuk aplikasi **Solarate** (clone Letterboxd) — UTS Pemrograman Aplikasi Bergerak.
FastAPI + TMDB. **Tanpa database**: backend hanya mengambil dan merapikan data film dari TMDB.
Data akun, log film, review, dan watchlist milik user disimpan sementara di sisi aplikasi Android
(hilang saat aplikasi di-restart).

## Setup (Anaconda, Python 3.12)

```bash
cd solarate-backend
conda env create -f environment.yml
conda activate solarate-backend
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Atau dobel-klik `run.bat` dari Anaconda Prompt. Token TMDB sudah ada di `.env`.

- Dokumentasi interaktif (coba endpoint langsung): http://localhost:8000/docs
- Cek koneksi ke TMDB: http://localhost:8000/health/tmdb
- Tes offline (tanpa internet): `python -m pytest -q`

## Menyambungkan ke Android

| Perangkat | Base URL di Retrofit |
|---|---|
| Emulator Android Studio | `http://10.0.2.2:8000/api/` |
| HP asli (satu WiFi dengan laptop) | `http://IP_LAPTOP:8000/api/` (cek `ipconfig`) |

Karena memakai `http` (bukan https), tambahkan di `AndroidManifest.xml`:
`android:usesCleartextTraffic="true"` pada tag `<application>`. Untuk HP asli, izinkan port 8000
di Windows Firewall (inbound rule).

## Endpoint

Semua prefix `/api`. Semua `GET`. Daftar film selalu berbentuk
`{page, total_pages, total_results, results[]}`. Rating memakai skala 0–5 seperti Letterboxd.

| Layar di aplikasi | Endpoint |
|---|---|
| Home > Films (Popular this week) | `/movies/trending?page=1` |
| Home > Reviews | `/reviews/popular?limit=20` |
| Home > Lists | `/lists`, `/lists/{slug}?page=1` |
| Home > Journal | `/journal`, `/journal/{slug}` |
| Search | `/search?q=...&type=all\|movie\|person` |
| Browse by (popular / top rated / anticipated) | `/movies/popular`, `/movies/top-rated`, `/movies/upcoming` |
| Browse by (release date / genre / country / language) | `/movies/discover?genre=&year=&decade=&country=&language=&sort_by=` |
| Isi filter Browse | `/meta/genres`, `/meta/countries`, `/meta/languages`, `/meta/sort-options` |
| Featured / Official lists | `/lists?official=false`, `/lists?official=true` |
| Film detail (cast, crew, trailer, where to watch, releases, similar, reviews) | `/movies/{id}?region=ID` |
| Semua review film | `/movies/{id}/reviews?page=1` |
| Related / similar films | `/movies/{id}/recommendations`, `/movies/{id}/similar` |
| Profil aktor/sutradara (tap cast) | `/people/{id}` |
| Sedang tayang | `/movies/now-playing?region=ID` |

Error selalu berbentuk `{"detail": "pesan"}` dengan status 404 / 422 / 502 / 503.

## Struktur

```
app/
  main.py        entry point, CORS, error handler, health check
  config.py      baca .env
  tmdb.py        client TMDB (retry + cache)
  cache.py       cache TTL in-memory
  mappers.py     TMDB -> JSON ramah Android
  schemas.py     model respons (muncul di /docs)
  curated.py     definisi lists & artikel Journal
  routers/       movies, search, people, meta, reviews, lists, journal, images
tests/test_smoke.py
```

## Troubleshooting

- **`/health/tmdb` error 502 / poster tidak muncul**: sebagian ISP di Indonesia memblokir
  `themoviedb.org` / `image.tmdb.org`. Coba ganti DNS (1.1.1.1 / 8.8.8.8) atau pakai VPN.
  Jika hanya poster yang bermasalah di HP, aktifkan baris `IMAGE_BASE_URL=.../img` di `.env`
  agar gambar lewat backend.
- **Token ditolak (401)**: token dan API key kamu sudah terlanjur ditempel di chat. Jika
  proyek di-push ke GitHub publik, **jangan commit `.env`** (sudah ada di `.gitignore`) dan
  generate ulang token di akun TMDB.
