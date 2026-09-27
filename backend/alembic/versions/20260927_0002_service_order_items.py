"""service order items (peças usadas em uma OS) + labor_amount

Revision ID: 0002
Revises: 0001
Create Date: 2026-09-27 12:00:00.000000
"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0002"
down_revision: str | None = "0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.add_column(
        "service_orders",
        sa.Column(
            "labor_amount",
            sa.Numeric(precision=12, scale=2),
            server_default=sa.text("0"),
            nullable=False,
        ),
    )
    op.create_table(
        "service_order_items",
        sa.Column("service_order_id", sa.Uuid(), nullable=False),
        sa.Column("part_id", sa.Uuid(), nullable=False),
        sa.Column("quantity", sa.Integer(), nullable=False),
        sa.Column("unit_price", sa.Numeric(precision=12, scale=2), nullable=False),
        sa.Column("id", sa.Uuid(), server_default=sa.text("gen_random_uuid()"), nullable=False),
        sa.Column(
            "created_at",
            sa.DateTime(timezone=True),
            server_default=sa.text("now()"),
            nullable=False,
        ),
        sa.ForeignKeyConstraint(
            ["service_order_id"],
            ["service_orders.id"],
            name=op.f("fk_service_order_items_service_order_id_service_orders"),
            ondelete="CASCADE",
        ),
        sa.ForeignKeyConstraint(
            ["part_id"], ["parts.id"], name=op.f("fk_service_order_items_part_id_parts")
        ),
        sa.PrimaryKeyConstraint("id", name=op.f("pk_service_order_items")),
    )
    op.create_index(
        op.f("ix_service_order_items_service_order_id"),
        "service_order_items",
        ["service_order_id"],
        unique=False,
    )
    op.create_index(
        op.f("ix_service_order_items_part_id"), "service_order_items", ["part_id"], unique=False
    )


def downgrade() -> None:
    op.drop_index(op.f("ix_service_order_items_part_id"), table_name="service_order_items")
    op.drop_index(
        op.f("ix_service_order_items_service_order_id"), table_name="service_order_items"
    )
    op.drop_table("service_order_items")
    op.drop_column("service_orders", "labor_amount")
