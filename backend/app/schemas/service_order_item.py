import uuid
from datetime import datetime
from decimal import Decimal

from pydantic import Field

from app.models.service_order_item import ServiceOrderItem
from app.schemas.common import CamelModel


class ServiceOrderItemRequest(CamelModel):
    part_id: uuid.UUID
    quantity: int = Field(gt=0)


class ServiceOrderItemResponse(CamelModel):
    id: uuid.UUID
    part_id: uuid.UUID
    part_name: str
    part_code: str
    quantity: int
    unit_price: float
    subtotal: float
    created_at: datetime

    @classmethod
    def from_model(cls, item: ServiceOrderItem) -> "ServiceOrderItemResponse":
        return cls(
            id=item.id,
            part_id=item.part_id,
            part_name=item.part.name,
            part_code=item.part.code,
            quantity=item.quantity,
            unit_price=item.unit_price,
            subtotal=item.subtotal,
            created_at=item.created_at,
        )


class LaborAmountRequest(CamelModel):
    labor_amount: Decimal = Field(ge=0, max_digits=12, decimal_places=2)
