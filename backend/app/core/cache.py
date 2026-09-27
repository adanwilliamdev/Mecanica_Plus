"""Redis: cache do dashboard e rate limit do login.

Tudo aqui é "fail-open": se o Redis estiver fora do ar, a API continua funcionando
(sem cache e sem rate limit) em vez de derrubar as requisições.
"""

import json
import logging
from typing import Any

from redis.asyncio import Redis
from redis.exceptions import RedisError

from app.core.config import get_settings

logger = logging.getLogger("mecanica.cache")

_redis: Redis | None = None


def get_redis() -> Redis:
    global _redis
    if _redis is None:
        _redis = Redis.from_url(
            get_settings().redis_url,
            decode_responses=True,
            socket_connect_timeout=1,
            socket_timeout=1,
        )
    return _redis


async def close_redis() -> None:
    global _redis
    if _redis is not None:
        await _redis.aclose()
        _redis = None


async def cache_get_json(key: str) -> Any | None:
    try:
        raw = await get_redis().get(key)
    except (RedisError, OSError) as exc:
        logger.warning("Redis indisponível (get %s): %s", key, exc)
        return None
    return json.loads(raw) if raw else None


async def cache_set_json(key: str, value: Any, ttl_seconds: int) -> None:
    try:
        await get_redis().set(key, json.dumps(value), ex=ttl_seconds)
    except (RedisError, OSError) as exc:
        logger.warning("Redis indisponível (set %s): %s", key, exc)


async def cache_delete(key: str) -> None:
    try:
        await get_redis().delete(key)
    except (RedisError, OSError) as exc:
        logger.warning("Redis indisponível (delete %s): %s", key, exc)


async def hit_rate_limit(key: str, limit: int, window_seconds: int) -> bool:
    """Conta uma tentativa e devolve True se o limite da janela já foi excedido."""
    try:
        redis = get_redis()
        count = await redis.incr(key)
        if count == 1:
            await redis.expire(key, window_seconds)
        return count > limit
    except (RedisError, OSError) as exc:
        logger.warning("Redis indisponível (rate limit): %s", exc)
        return False


async def ping_redis() -> bool:
    try:
        return bool(await get_redis().ping())
    except (RedisError, OSError):
        return False
