import uuid
from typing import Annotated

from fastapi import APIRouter, Query, status

from app.api.deps import SessionDep, require_roles
from app.models import Role, ServiceOrderStatus, User
from app.schemas.service_order import (
    ServiceOrderRequest,
    ServiceOrderResponse,
    StatusUpdateRequest,
)
from app.schemas.service_order_item import LaborAmountRequest, ServiceOrderItemRequest
from app.services import service_orders as service

router = APIRouter(prefix="/service-orders", tags=["Ordens de serviço"])

# MECHANIC só é liberado em GET /{id}, PATCH /{id}/status e nos itens/mão de obra; a
# restrição "somente a OS atribuída a ele" é aplicada em
# services.service_orders._assert_can_access, porque o controle por papel sozinho não
# sabe qual mecânico está por trás do ID da URL.
staff = require_roles(Role.ADMIN, Role.MANAGER, Role.RECEPTIONIST)
creators = require_roles(Role.ADMIN, Role.RECEPTIONIST)
viewers = require_roles(Role.ADMIN, Role.MANAGER, Role.RECEPTIONIST, Role.MECHANIC)
status_updaters = require_roles(Role.ADMIN, Role.MANAGER, Role.MECHANIC)
mechanics_only = require_roles(Role.MECHANIC)
billing_editors = require_roles(Role.ADMIN, Role.MANAGER, Role.RECEPTIONIST, Role.MECHANIC)

ViewerUser = Annotated[User, viewers]
StatusUser = Annotated[User, status_updaters]
MechanicUser = Annotated[User, mechanics_only]
BillingUser = Annotated[User, billing_editors]


@router.post(
    "",
    response_model=ServiceOrderResponse,
    status_code=status.HTTP_201_CREATED,
    dependencies=[creators],
)
async def create_service_order(data: ServiceOrderRequest, session: SessionDep):
    return await service.create_service_order(session, data)


@router.get("", response_model=list[ServiceOrderResponse], dependencies=[staff])
async def list_service_orders(session: SessionDep):
    return await service.list_service_orders(session)


# Precisa vir antes de "/{order_id}". A listagem geral é negada ao MECHANIC; esta devolve só
# as ordens atribuídas a ele.
@router.get("/mine", response_model=list[ServiceOrderResponse])
async def list_my_service_orders(session: SessionDep, user: MechanicUser):
    return await service.list_for_mechanic(session, user)


@router.get("/client/{client_id}", response_model=list[ServiceOrderResponse], dependencies=[staff])
async def list_by_client(client_id: uuid.UUID, session: SessionDep):
    return await service.list_by_client(session, client_id)


@router.get(
    "/vehicle/{vehicle_id}", response_model=list[ServiceOrderResponse], dependencies=[staff]
)
async def list_by_vehicle(vehicle_id: uuid.UUID, session: SessionDep):
    return await service.list_by_vehicle(session, vehicle_id)


@router.get(
    "/status/{order_status}", response_model=list[ServiceOrderResponse], dependencies=[staff]
)
async def list_by_status(order_status: ServiceOrderStatus, session: SessionDep):
    return await service.list_by_status(session, order_status)


@router.get("/{order_id}", response_model=ServiceOrderResponse)
async def get_service_order(order_id: uuid.UUID, session: SessionDep, user: ViewerUser):
    return await service.get_service_order_response(session, order_id, user)


@router.patch("/{order_id}/status", response_model=ServiceOrderResponse)
async def update_status(
    order_id: uuid.UUID, data: StatusUpdateRequest, session: SessionDep, user: StatusUser
):
    return await service.update_status(session, order_id, data, user)


@router.patch("/{order_id}/mechanic", response_model=ServiceOrderResponse, dependencies=[staff])
async def assign_mechanic(
    order_id: uuid.UUID,
    mechanic_id: Annotated[uuid.UUID, Query(alias="mechanicId")],
    session: SessionDep,
):
    return await service.assign_mechanic(session, order_id, mechanic_id)


@router.post(
    "/{order_id}/items", response_model=ServiceOrderResponse, status_code=status.HTTP_201_CREATED
)
async def add_item(
    order_id: uuid.UUID, data: ServiceOrderItemRequest, session: SessionDep, user: BillingUser
):
    """Registra o uso de uma peça na OS: baixa o estoque e soma ao total faturado."""
    return await service.add_item(session, order_id, data, user)


@router.delete("/{order_id}/items/{item_id}", response_model=ServiceOrderResponse)
async def remove_item(
    order_id: uuid.UUID, item_id: uuid.UUID, session: SessionDep, user: BillingUser
):
    """Remove uma peça lançada na OS e estorna a quantidade ao estoque."""
    return await service.remove_item(session, order_id, item_id, user)


@router.patch("/{order_id}/labor", response_model=ServiceOrderResponse)
async def update_labor_amount(
    order_id: uuid.UUID, data: LaborAmountRequest, session: SessionDep, user: BillingUser
):
    return await service.update_labor_amount(session, order_id, data.labor_amount, user)
