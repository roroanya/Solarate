"""Daftar (lists) dan artikel Journal buatan sendiri, di-generate dari query TMDB."""
import math
from typing import Any

from . import mappers
from . import schemas as s
from .tmdb import tmdb

PAGE_SIZE = 20
BASE_DISCOVER = {
    "include_adult": "false",
    "include_video": "false",
    "sort_by": "vote_average.desc",
}

# Genre ID TMDB: Animation 16, Crime 80, Comedy 35, Horror 27, Mystery 9648,
# Romance 10749, Science Fiction 878
LISTS: dict[str, dict[str, Any]] = {
    "top-rated-films": {
        "title": "Solarate Top Rated Films",
        "description": "Film dengan rating komunitas tertinggi sepanjang masa, diperbarui otomatis dari data TMDB.",
        "author": "Solarate Official",
        "official": True,
        "limit": 500,
        "endpoint": "/movie/top_rated",
    },
    "modern-must-watch": {
        "title": "Modern Must-Watch",
        "description": "Film-film yang ditonton dan dinilai sangat banyak orang, dan hampir selalu worth it.",
        "author": "Solarate Official",
        "official": True,
        "limit": 100,
        "params": {"vote_count.gte": 15000},
    },
    "horror-after-dark": {
        "title": "Horror After Dark",
        "description": "Pilihan horor terbaik untuk ditonton saat lampu dimatikan.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_genres": "27", "vote_count.gte": 1500},
    },
    "animation-gems": {
        "title": "Animation Gems",
        "description": "Animasi dengan cerita dan visual yang cocok untuk semua umur.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_genres": "16", "vote_count.gte": 1500},
    },
    "sci-fi-essentials": {
        "title": "Sci-Fi Essentials",
        "description": "Fiksi ilmiah yang wajib masuk daftar tonton, dari yang megah sampai yang bikin mikir.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_genres": "878", "vote_count.gte": 2000},
    },
    "crime-and-mystery": {
        "title": "Crime & Mystery",
        "description": "Kasus rumit, plot twist, dan karakter abu-abu.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_genres": "80|9648", "vote_count.gte": 2000},
    },
    "feel-good-romcom": {
        "title": "Feel-Good Romcom",
        "description": "Komedi romantis untuk memperbaiki mood di hari yang berat.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_genres": "10749,35", "vote_count.gte": 1000},
    },
    "hidden-gems": {
        "title": "Hidden Gems",
        "description": "Rating tinggi tapi belum terlalu ramai dibicarakan.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"vote_average.gte": 7.5, "vote_count.gte": 300, "vote_count.lte": 3000},
    },
    "nineties-classics": {
        "title": "Nineties Classics",
        "description": "Film terbaik dari dekade 90-an.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {
            "primary_release_date.gte": "1990-01-01",
            "primary_release_date.lte": "1999-12-31",
            "vote_count.gte": 2000,
        },
    },
    "korean-cinema": {
        "title": "Korean Cinema",
        "description": "Sinema Korea dengan rating tinggi.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_original_language": "ko", "vote_count.gte": 500},
    },
    "indonesian-cinema": {
        "title": "Sinema Indonesia",
        "description": "Film Indonesia pilihan dengan penilaian terbaik.",
        "author": "Solarate Curators",
        "official": False,
        "limit": 100,
        "params": {"with_original_language": "id", "vote_count.gte": 50},
    },
}

# Teks artikel ditulis sendiri (original). Daftar filmnya diambil dari list_slug.
JOURNAL: list[dict[str, Any]] = [
    {
        "slug": "hidden-gems-worth-the-detour",
        "title": "Hidden Gems Worth the Detour",
        "subtitle": "Film yang jarang masuk obrolan, padahal nilainya tinggi.",
        "list_slug": "hidden-gems",
        "paragraphs": [
            "Tidak semua film bagus lahir dengan kampanye promosi besar. Sebagian justru tumbuh pelan-pelan lewat rekomendasi dari mulut ke mulut dan rating penonton yang konsisten tinggi.",
            "Daftar ini dipilih dengan satu aturan sederhana: penilaian penonton tinggi, tetapi jumlah pemilai masih relatif sedikit. Artinya, kemungkinan besar film-film ini belum ada di radar kebanyakan orang.",
            "Coba mulai dari satu judul yang genre-nya paling jarang kamu tonton. Kejutan terbaik biasanya datang dari tempat yang tidak kita duga.",
        ],
    },
    {
        "slug": "after-dark-a-horror-starter-pack",
        "title": "After Dark: A Horror Starter Pack",
        "subtitle": "Panduan masuk ke dunia horor tanpa harus mulai dari yang paling ekstrem.",
        "list_slug": "horror-after-dark",
        "paragraphs": [
            "Horor adalah genre yang paling mudah dikenali reaksinya: ada yang menutup mata, ada yang tertawa gugup, ada yang tidak bisa berhenti menonton.",
            "Film-film di daftar ini dipilih berdasarkan penilaian penonton, bukan seberapa menakutkannya. Banyak di antaranya kuat karena atmosfer dan ceritanya, bukan sekadar jump scare.",
            "Tips kecil: tonton bersama teman untuk pengalaman pertama, lalu ulangi sendirian kalau berani.",
        ],
    },
    {
        "slug": "animation-is-not-just-for-kids",
        "title": "Animation Is Not Just for Kids",
        "subtitle": "Animasi adalah medium, bukan genre.",
        "list_slug": "animation-gems",
        "paragraphs": [
            "Animasi bisa menjadi komedi, drama keluarga, petualangan epik, sampai cerita yang sangat dewasa. Medium ini memberi sutradara kebebasan visual yang sulit dicapai film live-action.",
            "Pilihan di bawah ini mencakup berbagai gaya dan usia penonton. Semuanya punya penilaian yang konsisten bagus dari ribuan penonton.",
            "Kalau selama ini kamu melewatkan animasi karena menganggapnya hanya untuk anak-anak, daftar ini tempat yang pas untuk mengubah pikiran itu.",
        ],
    },
    {
        "slug": "built-for-the-big-screen-sci-fi",
        "title": "Built for the Big Screen: Sci-Fi Essentials",
        "subtitle": "Dunia lain, teknologi baru, dan pertanyaan besar tentang manusia.",
        "list_slug": "sci-fi-essentials",
        "paragraphs": [
            "Fiksi ilmiah punya cara unik untuk membicarakan hari ini lewat cerita tentang masa depan, planet lain, atau teknologi yang belum ada.",
            "Daftar ini memuat judul-judul dengan penilaian tinggi dan basis penonton besar, dari yang spektakuler secara visual sampai yang lebih tenang dan reflektif.",
            "Kalau bisa, tonton di layar terbesar dan speaker terbaik yang kamu punya.",
        ],
    },
    {
        "slug": "close-to-home-indonesian-cinema",
        "title": "Close to Home: Sinema Indonesia",
        "subtitle": "Cerita dari dekat, dengan rasa yang akrab.",
        "list_slug": "indonesian-cinema",
        "paragraphs": [
            "Perfilman Indonesia punya ragam yang luas: drama keluarga, horor lokal, aksi, hingga komedi yang dekat dengan keseharian.",
            "Daftar ini memuat film Indonesia dengan penilaian penonton yang baik. Cocok untuk memulai kalau kamu ingin lebih sering menonton produksi dalam negeri.",
            "Beri dukungan dengan menonton, menilai, dan menuliskan ulasanmu sendiri di Solarate.",
        ],
    },
]


async def fetch_list_page(slug: str, page: int) -> dict[str, Any]:
    """Ambil satu halaman isi list dari TMDB, lengkap dengan batas jumlah film."""
    defn = LISTS[slug]
    cap_pages = math.ceil(defn["limit"] / PAGE_SIZE)
    if page > cap_pages:
        return {"page": page, "total_pages": cap_pages, "total_results": 0, "results": []}

    if "endpoint" in defn:
        data = await tmdb.get(defn["endpoint"], {"page": page}, ttl=3600)
    else:
        params = {**BASE_DISCOVER, **defn["params"], "page": page}
        data = await tmdb.get("/discover/movie", params, ttl=3600)

    data = dict(data)  # jangan mutasi objek di cache
    data["total_pages"] = min(data.get("total_pages", 1), cap_pages)
    data["total_results"] = min(data.get("total_results", 0), defn["limit"])
    return data


def list_summary(slug: str, first_page: dict[str, Any]) -> s.ListSummary:
    defn = LISTS[slug]
    previews = [
        mappers.img(m["poster_path"], "w185")
        for m in first_page.get("results", [])
        if m.get("poster_path")
    ][:8]
    return s.ListSummary(
        slug=slug,
        title=defn["title"],
        description=defn["description"],
        author=defn["author"],
        official=defn["official"],
        film_count=first_page.get("total_results", 0),
        previews=previews,
    )
