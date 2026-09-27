import uuid
from datetime import datetime
from decimal import Decimal

from sqlalchemy import DateTime, ForeignKey, Integer, Numeric, Uuid, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base
from app.models.mixins import UUIDPrimaryKey
from app.models.part import Part


class ServiceOrderItem(UUIDPrimaryKey, Base):
    """Peça aplicada em uma OS. `unit_price` é uma cópia do preço da peça no momento do uso:
    o valor faturado não pode mudar retroativamente se o preço da peça for reajustado depois.
    """

    __tablename__ = "service_order_items"

    service_order_id: Mapped[uuid.UUID] = mapped_column(
        Uuid, ForeignKey("service_orders.id", ondelete="CASCADE"), index=True, nullable=False
    )
    part_id: Mapped[uuid.UUID] = mapped_column(
        Uuid, ForeignKey("parts.id"), index=True, nullable=False
    )
    quantity: Mapped[int] = mapped_column(Integer, nullable=False)
    unit_price: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), server_default=func.now(), nullable=False
    )

    part: Mapped[Part] = relationship(lazy="joined")

    @property
    def subtotal(self) -> Decimal:
        return self.unit_price * self.quantity
