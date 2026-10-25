# Solarate (Frontend)

Aplikasi mobile **Solarate**, clone Letterboxd, untuk UTS Pemrograman Aplikasi Bergerak.
Dibuat dengan Android (Java). Data film diambil dari backend FastAPI yang menjadi perantara ke TMDB
(lihat `solarate-backend`).

## Fitur

- **Home (Films):** film trending dari TMDB.
- **Journal:** daftar artikel dan detailnya.
- **Detail film:** poster, sinopsis, director, producer, cast, dan crew. Ada tombol Favorite,
  Watchlist, Watched, dan Review.
- **Review:** rating bintang (kelipatan 0,5), teks review, dan **tanggal nonton** lewat kalender.
- **Profile** (satu halaman dengan 4 tab: Profile, Diary, Lists, Watchlist):
    - **Profile:** foto, username, bio, favorit, recent activity, ringkasan Watched / Reviews / Watchlist.
    - **Diary:** film yang sudah ditonton, dikelompokkan per bulan berdasarkan tanggal nonton.
    - **Lists:** daftar list dari backend.
    - **Watchlist:** grid film yang ingin ditonton.
- **Settings:** ubah foto profil, username, bio, dan email.
- **Register / Login / Logout.**

## Teknologi

| Komponen | Versi / Library |
|---|---|
| Bahasa | Java |
| minSdk / targetSdk | 24 / 36 |
| Package | `com.example.solarate` |
| HTTP client | Retrofit 2.9.0 + Gson converter |
| Gambar | Glide 4.16.0 |
| UI | Material Components 1.11.0 (TabLayout, MaterialDatePicker, dialog) |

## Cara menjalankan

1. Jalankan **backend** dulu (lihat `solarate-backend/README.md`):
   ```bash
   cd solarate-backend
   conda activate solarate-backend
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
   Backend membaca token TMDB dari file `.env` di folder `solarate-backend/app/`.
   File itu tidak ikut GitHub, jadi buat sendiri berisi kunci TMDB.
2. Buka **Android Studio**, pilih **Open**, lalu pilih folder **`solarate-frontend`**
   (bukan folder induk `Solarate`, karena `settings.gradle.kts` ada di folder ini).
3. Tunggu Gradle sync selesai.
4. Jalankan di emulator (**Run > app**).

## Menyambungkan ke backend

Base URL diatur di `app/src/main/java/com/example/solarate/network/ApiClient.java`:

```java
public static final String BASE_URL = "http://10.0.2.2:8000/";
```

| Perangkat | Base URL |
|---|---|
| Emulator Android Studio | `http://10.0.2.2:8000/` |
| HP asli (satu WiFi dengan laptop) | `http://IP_LAPTOP:8000/` (cek dengan `ipconfig`) |

Untuk HP asli, izinkan port 8000 di Windows Firewall. Aplikasi memakai `http` (bukan https),
jadi `android:usesCleartextTraffic="true"` sudah diatur di `AndroidManifest.xml`.

## Penyimpanan data

Aplikasi **tidak memakai database**. Semua data user disimpan lokal di perangkat lewat SharedPreferences:

| File SharedPreferences | Isi |
|---|---|
| `SolaratePrefs` | Akun (username, password), bio, lokasi foto profil |
| `SolarateUserData` | Favorites, watchlist, watched, dan review per user (JSON, dikelola `UserDataManager`) |

Konsekuensinya, akun dan aktivitas hanya ada di perangkat itu. Kalau aplikasi di-uninstall atau datanya
di-clear, semuanya hilang, dan di perangkat baru harus **Register** dulu sebelum Login.

### Tanggal nonton (Diary)

Saat membuat review, user memilih tanggal nonton lewat kalender. Tanggal disimpan sebagai teks
`yyyy-MM-dd` di field `watchedDate` pada model `Review`:

- `null`: review lama, memakai tanggal review dibuat.
- `""` (kosong): tanggal dihapus user, review tidak muncul di Diary.

## Struktur folder

```
app/src/main/java/com/example/solarate/
├── activities/   # Splash, Login, Register, Main, MovieDetail, Reviews, Lists, Journal, Settings, dll.
├── fragments/    # FilmsFragment, JournalFragment, ProfileFragment (4 tab)
├── adapters/     # MovieAdapter, ReviewAdapter, DiaryAdapter, ListAdapter, JournalAdapter
├── data/         # UserDataManager (penyimpanan data user)
├── models/       # Movie, Review, ListSummary, JournalSummary, dll.
└── network/      # ApiClient (Retrofit) dan ApiService (endpoint)

app/src/main/res/layout/   # Layout XML
```

Navigasi bawah: **Home > Journal > Profile**.

## Catatan

- Kalau muncul error "Directory ... does not contain a Gradle build", berarti yang dibuka folder
  `Solarate`. Buka `solarate-frontend`.
- Kalau daftar film kosong atau error jaringan, pastikan backend sedang jalan dan base URL sesuai perangkat.