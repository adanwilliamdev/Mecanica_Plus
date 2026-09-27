from datetime import UTC, datetime

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.cache import cache_get_json, cache_set_json
from app.core.config import get_settings
from app.models import (
    Client,
    Mechanic,
    Part,
    ServiceOrder,
    ServiceOrderItem,
    ServiceOrderStatus,
    Vehicle,
)
from app.schemas.dashboard import DashboardStats, MonthlyRevenue, MostUsedPart, TopMechanic

CACHE_KEY = "dashboard:stats"
MONTH_LABELS = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"]


def _months_back(reference: datetime, count: int) -> list[datetime]:
    """Os últimos `count` meses (o 1º dia de cada um), do mais antigo ao mais recente,
    incluindo o mês de `reference`. Sem dependência extra: só aritmética de ano/mês."""
    start = reference.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
    result = []
    year, month = start.year, start.month
    for i in range(count - 1, -1, -1):
        m = month - i
        y = year + (m - 1) // 12
        m = (m - 1) % 12 + 1
        result.append(start.replace(year=y, month=m))
    return result


def _count_orders(*statuses: ServiceOrderStatus):
    return (
        select(func.count())
        .select_from(ServiceOrder)
        .where(ServiceOrder.status.in_(statuses))
        .scalar_subquery()
    )


async def _monthly_revenue_chart(session: AsyncSession) -> list[MonthlyRevenue]:
    """Soma o total faturado (OS finalizadas) mês a mês, para os últimos 6 meses.
    Meses sem OS finalizada entram com valor zero em vez de sumir do gráfico."""
    months = _months_back(datetime.now(UTC), 6)

    month_key = func.to_char(ServiceOrder.completed_at, "YYYY-MM")
    rows = await session.execute(
        select(month_key.label("month_key"), func.sum(ServiceOrder.total_amount))
        .where(
            ServiceOrder.status == ServiceOrderStatus.FINALIZADA,
            ServiceOrder.completed_at >= months[0],
        )
        .group_by("month_key")
    )
    totals_by_key = {key: float(total) for key, total in rows}

    return [
        MonthlyRevenue(
            month=MONTH_LABELS[m.month - 1],
            amount=totals_by_key.get(m.strftime("%Y-%m"), 0.0),
        )
        for m in months
    ]


async def _top_mechanics(session: AsyncSession, limit: int = 5) -> list[TopMechanic]:
    rows = await session.execute(
        select(Mechanic.id, Mechanic.name, func.count(ServiceOrder.id))
        .join(ServiceOrder, ServiceOrder.mechanic_id == Mechanic.id)
        .where(ServiceOrder.status == ServiceOrderStatus.FINALIZADA)
        .group_by(Mechanic.id, Mechanic.name)
        .order_by(func.count(ServiceOrder.id).desc())
        .limit(limit)
    )
    return [
        TopMechanic(mechanic_id=str(mid), mechanic_name=name, completed_orders=count)
        for mid, name, count in rows
    ]


async def _most_used_parts(session: AsyncSession, limit: int = 5) -> list[MostUsedPart]:
    rows = await session.execute(
        select(Part.id, Part.name, func.sum(ServiceOrderItem.quantity))
        .join(ServiceOrderItem, ServiceOrderItem.part_id == Part.id)
        .group_by(Part.id, Part.name)
        .order_by(func.sum(ServiceOrderItem.quantity).desc())
        .limit(limit)
    )
    return [
        MostUsedPart(part_id=str(pid), part_name=name, usage_count=int(count))
        for pid, name, count in rows
    ]


async def get_dashboard_stats(session: AsyncSession) -> DashboardStats:
    cached = await cache_get_json(CACHE_KEY)
    if cached is not None:
        return DashboardStats.model_validate(cached)

    S = ServiceOrderStatus
    row = (
        await session.execute(
            select(
                select(func.count()).select_from(Client).scalar_subquery(),
                select(func.count()).select_from(Vehicle).scalar_subquery(),
                select(func.count()).select_from(ServiceOrder).scalar_subquery(),
                _count_orders(S.CRIADA, S.EM_DIAGNOSTICO),
                _count_orders(S.EM_EXECUCAO),
                _count_orders(S.AGUARDANDO_APROVACAO),
                select(func.count())
                .select_from(Part)
                .where(Part.is_active.is_(True), Part.stock_quantity <= Part.minimum_stock)
                .scalar_subquery(),
            )
        )
    ).one()

    revenue_chart = await _monthly_revenue_chart(session)
    current_month_label = MONTH_LABELS[datetime.now(UTC).month - 1]
    monthly_revenue = next(
        (m.amount for m in revenue_chart if m.month == current_month_label), 0.0
    )

    stats = DashboardStats(
        total_clients=row[0],
        total_vehicles=row[1],
        total_service_orders=row[2],
        open_service_orders=row[3],
        in_progress_service_orders=row[4],
        waiting_approval_service_orders=row[5],
        low_stock_items=row[6],
        monthly_revenue=monthly_revenue,
        monthly_revenue_chart=revenue_chart,
        top_mechanics=await _top_mechanics(session),
        most_used_parts=await _most_used_parts(session),
    )
    await cache_set_json(
        CACHE_KEY,
        stats.model_dump(mode="json", by_alias=True),
        get_settings().dashboard_cache_ttl_seconds,
    )
    return stats
