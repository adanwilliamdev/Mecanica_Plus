"""Fixtures dos testes.

Os testes rodam contra um PostgreSQL real (o banco de testes é migrado com o Alembic,
então as migrations também são exercitadas) e um Redis em memória (fakeredis).

    createdb mecanica_test
    TEST_DATABASE_URL=postgresql+asyncpg://postgres:postgres@localhost:5432/mecanica_test pytest
"""

import os
import subprocess
import sys
import uuid
from pathlib import Path

# As variáveis precisam ser definidas ANTES de importar a aplicação.
TEST_DATABASE_URL = os.environ.get(
    "TEST_DATABASE_URL", "postgresql+asyncpg://postgres:postgres@localhost:5432/mecanica_test"
)
os.environ["DATABASE_URL"] = TEST_DATABASE_URL
os.environ["ENVIRONMENT"] = "test"
os.environ["JWT_SECRET"] = "segredo-de-teste-com-mais-de-32-caracteres-ok"
os.environ["LOGIN_RATE_LIMIT_ATTEMPTS"] = "5"

import httpx  # noqa: E402
import pytest  # noqa: E402
import pytest_asyncio  # noqa: E402
from fakeredis import FakeAsyncRedis  # noqa: E402
from sqlalchemy import text  # noqa: E402

from app.core import cache  # noqa: E402
from app.core.security import hash_password  # noqa: E402
from app.db.session import SessionLocal, engine  # noqa: E402
from app.main import app  # noqa: E402
from app.models import Client, Mechanic, Role, User, Vehicle  # noqa: E402

BACKEND_DIR = Path(__file__).resolve().parents[1]
PASSWORD = "senha-segura-123"


@pytest.fixture(scope="session", autouse=True)
def _migrate_database() -> None:
    subprocess.run(
        [sys.executable, "-m", "alembic", "upgrade", "head"],
        cwd=BACKEND_DIR,
        env=os.environ.copy(),
        check=True,
    )


@pytest_asyncio.fixture(autouse=True)
async def _clean_state():
    cache._redis = FakeAsyncRedis(decode_responses=True)
    async with engine.begin() as conn:
        await conn.execute(
            text(
                "TRUNCATE inventory_movements, budgets, service_order_items, service_orders, "
                "mechanics, vehicles, clients, parts, refresh_tokens, users "
                "RESTART IDENTITY CASCADE"
            )
        )
        await conn.execute(text("ALTER SEQUENCE service_order_number_seq RESTART"))
        await conn.execute(text("ALTER SEQUENCE budget_number_seq RESTART"))
    yield
    await cache._redis.aclose()
    cache._redis = None


@pytest_asyncio.fixture
async def api():
    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="http://test/api") as client:
        yield client


@pytest_asyncio.fixture
async def session():
    async with SessionLocal() as s:
        yield s


async def create_user(role: Role, email: str | None = None, active: bool = True) -> User:
    async with SessionLocal() as s:
        user = User(
            name=f"Usuário {role.value.title()}",
            email=email or f"{role.value.lower()}-{uuid.uuid4().hex[:6]}@example.com",
            password_hash=await hash_password(PASSWORD),
            role=role,
            is_active=active,
        )
        s.add(user)
        await s.commit()
        return user


async def login_headers(api: httpx.AsyncClient, user: User) -> dict[str, str]:
    resp = await api.post("/auth/login", json={"email": user.email, "password": PASSWORD})
    assert resp.status_code == 200, resp.text
    return {"Authorization": f"Bearer {resp.json()['token']}"}


@pytest_asyncio.fixture
async def admin(api) -> dict[str, str]:
    return await login_headers(api, await create_user(Role.ADMIN))


@pytest_asyncio.fixture
async def headers_for(api):
    """Devolve uma função role -> headers (cria um usuário daquele papel)."""

    async def factory(role: Role) -> dict[str, str]:
        return await login_headers(api, await create_user(role))

    return factory


@pytest_asyncio.fixture
async def client_and_vehicle(session):
    client = Client(name="Maria Souza")
    session.add(client)
    await session.flush()
    vehicle = Vehicle(plate="ABC1D23", brand="VW", model="Gol", year=2020, client_id=client.id)
    session.add(vehicle)
    await session.commit()
    return client, vehicle


@pytest_asyncio.fixture
async def mechanic_profile(session):
    """Cria um usuário MECHANIC com perfil de mecânico vinculado."""
    user = await create_user(Role.MECHANIC)
    mechanic = Mechanic(name="João Mecânico", user_id=user.id)
    session.add(mechanic)
    await session.commit()
    return user, mechanic
