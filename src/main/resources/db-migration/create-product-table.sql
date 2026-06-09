CREATE TABLE IF NOT EXISTS product (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID           NOT NULL REFERENCES product_category(id) ON DELETE RESTRICT,
    name        VARCHAR(150)   NOT NULL,
    description VARCHAR(500),
    quantity    INTEGER        NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    sku         VARCHAR(100)   UNIQUE,
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
    );