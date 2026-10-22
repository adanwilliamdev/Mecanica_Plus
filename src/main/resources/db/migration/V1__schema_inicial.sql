-- Numeração atômica de OS e orçamentos
CREATE SEQUENCE service_order_number_seq;
CREATE SEQUENCE budget_number_seq;

CREATE TABLE clients (
    id          uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(255) NOT NULL,
    cpf         varchar(255),
    phone       varchar(255),
    email       varchar(255),
    address     varchar(255),
    is_active   boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_clients_cpf UNIQUE (cpf)
);

CREATE TABLE parts (
    id              uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(255)  NOT NULL,
    code            varchar(255)  NOT NULL,
    manufacturer    varchar(255),
    purchase_price  numeric(12,2) NOT NULL,
    sale_price      numeric(12,2) NOT NULL,
    stock_quantity  integer       NOT NULL DEFAULT 0,
    minimum_stock   integer       NOT NULL DEFAULT 5,
    is_active       boolean       NOT NULL DEFAULT true,
    version         integer       NOT NULL DEFAULT 0,
    created_at      timestamptz   NOT NULL DEFAULT now(),
    updated_at      timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT uq_parts_code UNIQUE (code),
    CONSTRAINT ck_parts_stock_non_negative CHECK (stock_quantity >= 0)
);

CREATE TABLE users (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name           varchar(255) NOT NULL,
    email          varchar(255) NOT NULL,
    password_hash  varchar(255) NOT NULL,
    role           varchar(20)  NOT NULL,
    is_active      boolean      NOT NULL DEFAULT true,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'RECEPTIONIST', 'MECHANIC', 'MANAGER'))
);

CREATE TABLE inventory_movements (
    id                uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    part_id           uuid         NOT NULL REFERENCES parts (id),
    type              varchar(20)  NOT NULL,
    quantity          integer      NOT NULL,
    previous_quantity integer      NOT NULL,
    current_quantity  integer      NOT NULL,
    reason            varchar(255),
    reference_id      varchar(255),
    user_id           uuid         REFERENCES users (id) ON DELETE SET NULL,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_inventory_movements_type CHECK (type IN ('ENTRADA', 'SAIDA', 'AJUSTE'))
);
CREATE INDEX ix_inventory_movements_part_id ON inventory_movements (part_id);

CREATE TABLE mechanics (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       uuid         REFERENCES users (id) ON DELETE SET NULL,
    name          varchar(255) NOT NULL,
    specialty     varchar(255),
    phone         varchar(255),
    is_available  boolean      NOT NULL DEFAULT true,
    is_active     boolean      NOT NULL DEFAULT true,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_mechanics_user_id UNIQUE (user_id)
);

CREATE TABLE refresh_tokens (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    token_hash  varchar(64) NOT NULL,
    user_id     uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at  timestamptz NOT NULL,
    revoked     boolean     NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE vehicles (
    id          uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    plate       varchar(255) NOT NULL,
    brand       varchar(255) NOT NULL,
    model       varchar(255) NOT NULL,
    year        integer      NOT NULL,
    mileage     integer,
    fuel_type   varchar(255),
    is_active   boolean      NOT NULL DEFAULT true,
    client_id   uuid         NOT NULL REFERENCES clients (id),
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT uq_vehicles_plate UNIQUE (plate)
);
CREATE INDEX ix_vehicles_client_id ON vehicles (client_id);

CREATE TABLE service_orders (
    id               uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number     varchar(255)  NOT NULL,
    client_id        uuid          NOT NULL REFERENCES clients (id),
    vehicle_id       uuid          NOT NULL REFERENCES vehicles (id),
    mechanic_id      uuid          REFERENCES mechanics (id),
    reported_problem varchar,
    diagnosis        varchar,
    status           varchar(30)   NOT NULL DEFAULT 'CRIADA',
    labor_amount     numeric(12,2) NOT NULL DEFAULT 0,
    total_amount     numeric(12,2) NOT NULL DEFAULT 0,
    started_at       timestamptz,
    completed_at     timestamptz,
    created_at       timestamptz   NOT NULL DEFAULT now(),
    updated_at       timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT uq_service_orders_order_number UNIQUE (order_number),
    CONSTRAINT ck_service_orders_status CHECK (status IN
        ('CRIADA', 'EM_DIAGNOSTICO', 'AGUARDANDO_APROVACAO', 'APROVADA',
         'EM_EXECUCAO', 'FINALIZADA', 'CANCELADA'))
);
CREATE INDEX ix_service_orders_client_id   ON service_orders (client_id);
CREATE INDEX ix_service_orders_mechanic_id ON service_orders (mechanic_id);
CREATE INDEX ix_service_orders_status      ON service_orders (status);
CREATE INDEX ix_service_orders_vehicle_id  ON service_orders (vehicle_id);

CREATE TABLE service_order_items (
    id                uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    service_order_id  uuid          NOT NULL REFERENCES service_orders (id) ON DELETE CASCADE,
    part_id           uuid          NOT NULL REFERENCES parts (id),
    quantity          integer       NOT NULL,
    unit_price        numeric(12,2) NOT NULL,
    created_at        timestamptz   NOT NULL DEFAULT now()
);
CREATE INDEX ix_service_order_items_service_order_id ON service_order_items (service_order_id);
CREATE INDEX ix_service_order_items_part_id          ON service_order_items (part_id);

CREATE TABLE budgets (
    id                uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_number     varchar(255)  NOT NULL,
    client_id         uuid          NOT NULL REFERENCES clients (id),
    vehicle_id        uuid          NOT NULL REFERENCES vehicles (id),
    service_order_id  uuid          REFERENCES service_orders (id),
    description       varchar,
    total_amount      numeric(12,2) NOT NULL,
    status            varchar(20)   NOT NULL DEFAULT 'PENDENTE',
    valid_until       timestamptz,
    created_at        timestamptz   NOT NULL DEFAULT now(),
    updated_at        timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT uq_budgets_budget_number UNIQUE (budget_number),
    CONSTRAINT uq_budgets_service_order_id UNIQUE (service_order_id),
    CONSTRAINT ck_budgets_status CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO', 'EXPIRADO'))
);
CREATE INDEX ix_budgets_client_id  ON budgets (client_id);
CREATE INDEX ix_budgets_status     ON budgets (status);
CREATE INDEX ix_budgets_vehicle_id ON budgets (vehicle_id);
