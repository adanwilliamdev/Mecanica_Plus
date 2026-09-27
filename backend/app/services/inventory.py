import uuid

from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.exceptions import (
    BusinessError,
    InsufficientStockError,
    NotFoundError,
    OptimisticLockError,
)
from app.models import InventoryMovement, MovementType, Part
from app.schemas.inventory import MovementResponse, PartRequest, PartResponse


async def get_part(session: AsyncSession, part_id: uuid.UUID) -> Part:
    part = await session.get(Part, part_id)
    if part is None:
        raise NotFoundError(f"Peça não encontrada com ID: {part_id}")
    return part


async def _assert_code_free(session: AsyncSession, code: str) -> None:
    if await session.scalar(select(Part.id).where(Part.code == code)):
        raise BusinessError(f"Já existe uma peça com este código: {code}")


async def create_part(session: AsyncSession, data: PartRequest, user_id: uuid.UUID) -> PartResponse:
    await _assert_code_free(session, data.code)

    part = Part(**data.model_dump(), is_active=True)
    session.add(part)
    await session.flush()
    session.add(
        InventoryMovement(
            part_id=part.id,
            type=MovementType.ENTRADA,
            quantity=data.stock_quantity,
            previous_quantity=0,
            current_quantity=data.stock_quantity,
            reason="Cadastro inicial",
            user_id=user_id,
        )
    )
    await session.commit()
    return PartResponse.from_model(part)


async def update_part(session: AsyncSession, part_id: uuid.UUID, data: PartRequest) -> PartResponse:
    part = await get_part(session, part_id)
    if part.code != data.code:
        await _assert_code_free(session, data.code)

    # O estoque só muda por movimentações (add/remove), nunca por edição do cadastro.
    part.name = data.name
    part.code = data.code
    part.manufacturer = data.manufacturer
    part.purchase_price = data.purchase_price
    part.sale_price = data.sale_price
    part.minimum_stock = data.minimum_stock
    await session.commit()
    await session.refresh(part)
    return PartResponse.from_model(part)


async def deactivate_part(session: AsyncSession, part_id: uuid.UUID) -> None:
    part = await get_part(session, part_id)
    part.is_active = False
    await session.commit()


async def list_parts(session: AsyncSession) -> list[PartResponse]:
    result = await session.scalars(
        select(Part).where(Part.is_active.is_(True)).order_by(Part.created_at.desc())
    )
    return [PartResponse.from_model(p) for p in result]


async def list_low_stock_parts(session: AsyncSession) -> list[PartResponse]:
    result = await session.scalars(
        select(Part).where(Part.is_active.is_(True), Part.stock_quantity <= Part.minimum_stock)
    )
    return [PartResponse.from_model(p) for p in result]


async def get_part_response(session: AsyncSession, part_id: uuid.UUID) -> PartResponse:
    return PartResponse.from_model(await get_part(session, part_id))


async def _apply_movement(
    session: AsyncSession,
    part_id: uuid.UUID,
    delta: int,
    reason: str | None,
    user_id: uuid.UUID,
    movement_type: MovementType,
    reference_id: str | None = None,
) -> None:
    part = await get_part(session, part_id)
    new_quantity = part.stock_quantity + delta
    if new_quantity < 0:
        raise InsufficientStockError(f"Estoque insuficiente. Disponível: {part.stock_quantity}")

    # Lock otimista: o UPDATE só acontece se a versão ainda for a que lemos. Se outra
    # requisição alterou a peça no meio do caminho, nenhuma linha é afetada -> 409.
    result = await session.execute(
        update(Part)
        .where(Part.id == part_id, Part.version == part.version)
        .values(stock_quantity=new_quantity, version=Part.version + 1)
        .execution_options(synchronize_session=False)
    )
    if result.rowcount == 0:
        await session.rollback()
        raise OptimisticLockError()

    session.add(
        InventoryMovement(
            part_id=part_id,
            type=movement_type,
            quantity=abs(delta),
            previous_quantity=part.stock_quantity,
            current_quantity=new_quantity,
            reason=reason,
            reference_id=reference_id,
            user_id=user_id,
        )
    )
    await session.commit()


async def add_stock(
    session: AsyncSession,
    part_id: uuid.UUID,
    quantity: int,
    reason: str | None,
    user_id: uuid.UUID,
    reference_id: str | None = None,
) -> None:
    if quantity <= 0:
        raise BusinessError("Quantidade deve ser positiva")
    await _apply_movement(
        session, part_id, quantity, reason, user_id, MovementType.ENTRADA, reference_id
    )


async def remove_stock(
    session: AsyncSession,
    part_id: uuid.UUID,
    quantity: int,
    reason: str | None,
    user_id: uuid.UUID,
    reference_id: str | None = None,
) -> None:
    if quantity <= 0:
        raise BusinessError("Quantidade deve ser positiva")
    await _apply_movement(
        session, part_id, -quantity, reason, user_id, MovementType.SAIDA, reference_id
    )


async def list_movements(session: AsyncSession, part_id: uuid.UUID) -> list[MovementResponse]:
    await get_part(session, part_id)
    result = await session.scalars(
        select(InventoryMovement)
        .where(InventoryMovement.part_id == part_id)
        .order_by(InventoryMovement.created_at.desc())
    )
    return [MovementResponse.from_model(m) for m in result]
