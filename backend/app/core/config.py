from functools import lru_cache

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

PLACEHOLDER_JWT_SECRET = "troque-este-segredo-em-producao-para-uma-string-longa-e-aleatoria"


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    environment: str = "development"

    database_url: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/mecanica"
    redis_url: str = "redis://localhost:6379/0"

    jwt_secret: str = PLACEHOLDER_JWT_SECRET
    jwt_algorithm: str = "HS256"
    jwt_expiration_ms: int = 86_400_000
    refresh_token_days: int = 7

    cors_allowed_origins: str = "http://localhost:3000"

    login_rate_limit_attempts: int = 10
    login_rate_limit_window_seconds: int = 60
    dashboard_cache_ttl_seconds: int = 30

    seed_admin_email: str = "admin@mecanicaplus.com"
    seed_admin_password: str = "admin123"

    @model_validator(mode="after")
    def _normalize_and_check(self) -> "Settings":
        # Provedores como Render/Heroku entregam "postgres://" ou "postgresql://";
        # o SQLAlchemy async precisa do driver explícito.
        url = self.database_url
        for prefix in ("postgres://", "postgresql://"):
            if url.startswith(prefix):
                self.database_url = "postgresql+asyncpg://" + url[len(prefix) :]
                break

        if self.environment == "production" and (
            self.jwt_secret == PLACEHOLDER_JWT_SECRET or len(self.jwt_secret) < 32
        ):
            raise ValueError(
                "JWT_SECRET precisa ser definido com um valor forte (32+ caracteres) em produção"
            )
        return self

    @property
    def cors_origins(self) -> list[str]:
        return [o.strip() for o in self.cors_allowed_origins.split(",") if o.strip()]

    @property
    def access_token_ttl_seconds(self) -> int:
        return self.jwt_expiration_ms // 1000


@lru_cache
def get_settings() -> Settings:
    return Settings()
