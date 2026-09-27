import uuid
from datetime import datetime

from app.models.enums import ServiceOrderStatus
from app.models.service_order import ServiceOrder
from app.schemas.common import CamelModel
from app.schemas.service_order_item import ServiceOrderItemResponse


class ServiceOrderRequest(CamelModel):
    client_id: uuid.UUID
    vehicle_id: uuid.UUID
    mechanic_id: uuid.UUID | None = None
    reported_problem: str | None = None


class StatusUpdateRequest(CamelModel):
    status: ServiceOrderStatus
    diagnosis: str | None = None


class ServiceOrderResponse(CamelModel):
    id: uuid.UUID
    order_number: str
    client_id: uuid.UUID
    client_name: str
    vehicle_id: uuid.UUID
    vehicle_info: str
    mechanic_id: uuid.UUID | None
    mechanic_name: str | None
    reported_problem: str | None
    diagnosis: str | None
    status: ServiceOrderStatus
    labor_amount: float
    parts_amount: float
    total_amount: float
    items: list[ServiceOrderItemResponse]
    started_at: datetime | None
    completed_at: datetime | None
    created_at: datetime

    @classmethod
    def from_model(cls, o: ServiceOrder) -> "ServiceOrderResponse":
        v = o.vehicle
        items = [ServiceOrderItemResponse.from_model(i) for i in o.items]
        return cls(
            id=o.id,
            order_number=o.order_number,
            client_id=o.client_id,
            client_name=o.client.name,
            vehicle_id=o.vehicle_id,
            vehicle_info=f"{v.brand} {v.model} ({v.plate})",
            mechanic_id=o.mechanic_id,
            mechanic_name=o.mechanic.name if o.mechanic else None,
            reported_problem=o.reported_problem,
            diagnosis=o.diagnosis,
            status=o.status,
            labor_amount=o.labor_amount,
            parts_amount=sum((i.subtotal for i in items), 0.0),
            total_amount=o.total_amount,
            items=items,
            started_at=o.started_at,
            completed_at=o.completed_at,
            created_at=o.created_at,
        )
