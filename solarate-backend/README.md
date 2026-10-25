# Solarate Movie API (Backend)

Backend untuk aplikasi **Solarate** (clone Letterboxd), UTS Pemrograman Aplikasi Bergerak.
FastAPI + TMDB. **Tanpa database**: backend hanya mengambil dan merapikan data film dari TMDB.

Data akun, favorites, watchlist, watched, review, dan tanggal nonton milik user **tidak** disimpan di backend.
Semuanya disimpan lokal di perangkat Android lewat SharedPreferences, jadi tetap ada setelah aplikasi
di-restart dan baru hilang kalau aplikasi di-uninstall atau datanya di-clear.

## Setup (Anaconda, Python 3.12)

```bash
cd solarate-backend
conda env create -f environment.yml
conda activate solarate-backend
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Atau dobel-klik `run.bat` dari Anaconda Prompt.

### File `.env`

Buat file `.env` di folder `solarate-backend` (sejajar dengan `requirements.txt`) berisi kunci TMDB.
Contoh formatnya ada di `.env.example`. File `.env` sengaja tidak ikut GitHub.

- Dokumentasi interaktif (coba endpoint langsung): http://localhost:8000/docs
- Cek koneksi ke TMDB: http://localhost:8000/health/tmdb
- Tes offline (tanpa internet): `python -m pytest -q`

## Menyambungkan ke Android

Base URL diatur di `ApiClient.java` pada frontend. Path endpoint (`api/movies/...`) sudah ditulis di
`ApiService`, jadi base URL **tidak** diakhiri `/api/`.

| Perangkat | Base URL di Retrofit |
|---|---|
| Emulator Android Studio | `http://10.0.2.2:8000/` |
| HP asli (satu WiFi dengan laptop) | `http://IP_LAPTOP:8000/` (cek `ipconfig`) |

Karena memakai `http` (bukan https), `AndroidManifest.xml` perlu `android:usesCleartextTraffic="true"`
pada tag `<application>`. Untuk HP asli, izinkan port 8000 di Windows Firewall (inbound rule).

## Endpoint

Semua prefix `/api` dan semua `GET`. Daftar film selalu berbentuk
`{page, total_pages, total_results, results[]}`. Rating memakai skala 0-5 seperti Letterboxd.

### Dipakai aplikasi saat ini

| Layar di aplikasi | Endpoint |
|---|---|
| Home (Films, trending hari ini) | `/movies/trending?window=day&page=1` |
| Home (popular) | `/movies/popular?page=1` |
| Film detail (cast, crew, director, producer, trailer, where to watch, similar, reviews) | `/movies/{id}?region=ID` |
| Journal | `/journal`, `/journal/{slug}` |
| Profile > Lists dan detail list | `/lists`, `/lists/{slug}?page=1` |

Parameter `window` pada trending bernilai `day` atau `week`.
Detail film menyertakan `director` dan `producer` (teks nama, dipisah koma) selain daftar `cast` dan `crew`.

### Tersedia tapi belum dipakai di UI

| Fungsi | Endpoint |
|---|---|
| Review populer | `/reviews/popular?limit=20` |
| Search | `/search?q=...&type=all\|movie\|person` |
| Browse (popular / top rated / anticipated) | `/movies/popular`, `/movies/top-rated`, `/movies/upcoming` |
| Browse (release date / genre / country / language) | `/movies/discover?genre=&year=&decade=&country=&language=&sort_by=` |
| Isi filter Browse | `/meta/genres`, `/meta/countries`, `/meta/languages`, `/meta/sort-options` |
| Featured / Official lists | `/lists?official=false`, `/lists?official=true` |
| Semua review satu film | `/movies/{id}/reviews?page=1` |
| Related / similar films | `/movies/{id}/recommendations`, `/movies/{id}/similar` |
| Profil aktor / sutradara | `/people/{id}` |
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
- **Token ditolak (401)**: periksa isi `.env`. Jangan commit `.env` ke GitHub (sudah ada di
  `.gitignore`). Kalau token pernah terlanjur ke-upload atau ditempel di chat, generate ulang token
  di akun TMDB.
- **IndentationError saat menjalankan uvicorn**: biasanya ada baris yang tidak sejajar di
  `app/mappers.py`. Cek indentasinya.