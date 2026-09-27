import uuid
from datetime import datetime
from decimal import Decimal

from sqlalchemy import DateTime, Enum, ForeignKey, Numeric, Sequence, String, Uuid, text
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.models.client import Client
from app.models.enums import ServiceOrderStatus
from app.models.mechanic import Mechanic
from app.models.mixins import Timestamps, UUIDPrimaryKey
from app.models.service_order_item import ServiceOrderItem
from app.models.vehicle import Vehicle

# Números de OS e orçamento vêm de sequences nativas do Postgres: atômicas sob concorrência.
service_order_number_seq = Sequence("service_order_number_seq", metadata=Base.metadata)


class ServiceOrder(UUIDPrimaryKey, Timestamps, Base):
    __tablename__ = "service_orders"

    order_number: Mapped[str] = mapped_column(String, unique=True, nullable=False)
    client_id: Mapped[uuid.UUID] = mapped_column(
        Uuid, ForeignKey("clients.id"), index=True, nullable=False
    )
    vehicle_id: Mapped[uuid.UUID] = mapped_column(
        Uuid, ForeignKey("vehicles.id"), index=True, nullable=False
    )
    mechanic_id: Mapped[uuid.UUID | None] = mapped_column(
        Uuid, ForeignKey("mechanics.id"), index=True
    )
    reported_problem: Mapped[str | None] = mapped_column(String)
    diagnosis: Mapped[str | None] = mapped_column(String)
    status: Mapped[ServiceOrderStatus] = mapped_column(
        Enum(ServiceOrderStatus, name="service_order_status"),
        default=ServiceOrderStatus.CRIADA,
        server_default=ServiceOrderStatus.CRIADA.value,
        index=True,
        nullable=False,
    )
    # Mão de obra: valor lançado manualmente pela oficina, somado ao total das peças
    # para compor `total_amount`. Ambos são recalculados em app.services.service_orders.
    labor_amount: Mapped[Decimal] = mapped_column(
        Numeric(12, 2), default=Decimal("0"), server_default=text("0"), nullable=False
    )
    total_amount: Mapped[Decimal] = mapped_column(
        Numeric(12, 2), default=Decimal("0"), server_default=text("0"), nullable=False
    )
    started_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

    client: Mapped[Client] = relationship(lazy="joined")
    vehicle: Mapped[Vehicle] = relationship(lazy="joined")
    mechanic: Mapped[Mechanic | None] = relationship(lazy="joined")
    items: Mapped[list[ServiceOrderItem]] = relationship(
        lazy="selectin", order_by=ServiceOrderItem.created_at, cascade="all, delete-orphan"
    )
