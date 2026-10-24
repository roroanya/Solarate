from typing import Generic, TypeVar
from pydantic import BaseModel
T = TypeVar("T")

class Page(BaseModel, Generic[T]):
    page: int
    total_pages: int
    total_results: int
    results: list[T]

class MovieSummary(BaseModel):
    id: int
    title: str
    year: int | None = None
    release_date: str | None = None
    poster_url: str | None = None
    backdrop_url: str | None = None
    rating: float | None = None
    vote_count: int = 0

class Genre(BaseModel):
    id: int
    name: str

class CastMember(BaseModel):
    id: int
    name: str
    character: str | None = None
    profile_url: str | None = None

class CrewMember(BaseModel):
    id: int
    name: str
    role: str
    profile_url: str | None = None

class Provider(BaseModel):
    id: int
    name: str
    logo_url: str | None = None

class Providers(BaseModel):
    region: str | None = None
    link: str | None = None
    flatrate: list[Provider] = []
    rent: list[Provider] = []
    buy: list[Provider] = []

class Release(BaseModel):
    country: str
    type: str
    date: str | None = None
    certification: str | None = None

class Review(BaseModel):
    id: str
    author: str
    username: str | None = None
    avatar_url: str | None = None
    rating: float | None = None  # bintang 0-5 (kelipatan 0.5)
    content: str
    excerpt: str
    created_at: str | None = None
    url: str | None = None
    movie: MovieSummary | None = None

class MovieDetail(BaseModel):
    id: int
    title: str
    original_title: str | None = None
    tagline: str | None = None
    overview: str | None = None
    year: int | None = None
    release_date: str | None = None
    runtime: int | None = None
    status: str | None = None
    genres: list[Genre] = []
    poster_url: str | None = None
    backdrop_url: str | None = None
    rating: float | None = None
    vote_count: int = 0
    directors: list[str] = []
    director: str | None = None
    producer: str | None = None
    cast: list[CastMember] = []
    crew: list[CrewMember] = []
    trailer_url: str | None = None
    trailer_youtube_key: str | None = None
    studios: list[str] = []
    countries: list[str] = []
    languages: list[str] = []
    original_language: str | None = None
    certification: str | None = None
    providers: Providers = Providers()
    releases: list[Release] = []
    similar: list[MovieSummary] = []
    recommendations: list[MovieSummary] = []
    reviews: list[Review] = []

class PersonSummary(BaseModel):
    id: int
    name: str
    profile_url: str | None = None
    known_for_department: str | None = None

class PersonDetail(PersonSummary):
    biography: str | None = None
    birthday: str | None = None
    deathday: str | None = None
    place_of_birth: str | None = None
    films_as_actor: list[MovieSummary] = []
    films_as_director: list[MovieSummary] = []

class SearchResponse(BaseModel):
    query: str
    movies: Page[MovieSummary] | None = None
    people: Page[PersonSummary] | None = None

class ListSummary(BaseModel):
    slug: str
    title: str
    description: str
    author: str
    official: bool
    film_count: int
    previews: list[str]

class ListDetail(BaseModel):
    slug: str
    title: str
    description: str
    author: str
    official: bool
    film_count: int
    films: Page[MovieSummary]

class JournalSummary(BaseModel):
    slug: str
    title: str
    subtitle: str
    cover_urls: list[str]

class JournalDetail(JournalSummary):
    paragraphs: list[str]
    list_slug: str
    films: list[MovieSummary]

class Country(BaseModel):
    code: str
    name: str

class Language(BaseModel):
    code: str
    name: str

class SortOption(BaseModel):
    key: str
    label: str