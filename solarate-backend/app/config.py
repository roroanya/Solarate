from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

ENV_FILE = Path(__file__).resolve().parent.parent / ".env"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=str(ENV_FILE), env_file_encoding="utf-8", extra="ignore"
    )

    tmdb_read_token: str = ""
    tmdb_api_key: str = ""
    tmdb_base_url: str = "https://api.themoviedb.org/3"
    tmdb_image_origin: str = "https://image.tmdb.org"
    image_base_url: str = "https://image.tmdb.org/t/p"

    default_language: str = "en-US"
    default_region: str = "ID"
    cache_ttl_seconds: int = 600
    request_timeout: float = 10.0


settings = Settings()
