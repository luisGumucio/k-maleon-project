-- K-Maleon — Schema inicial
-- Correr en Supabase SQL Editor (en orden)

-- =====================
-- 1. SUPPLIERS
-- =====================
CREATE TABLE IF NOT EXISTS suppliers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- =====================
-- 2. ACCOUNTS
-- =====================
CREATE TABLE IF NOT EXISTS accounts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    balance    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- =====================
-- 3. OPERATIONS
-- =====================
CREATE TABLE IF NOT EXISTS operations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id   UUID NOT NULL REFERENCES suppliers(id),
    container     VARCHAR(100) NOT NULL,
    description   TEXT,
    total_amount  BIGINT NOT NULL,
    paid_amount   BIGINT DEFAULT 0,
    origin        VARCHAR(100),
    start_date    DATE NOT NULL,
    end_date      DATE,
    status        VARCHAR(50) DEFAULT 'active',
    notes         TEXT,
    created_at    TIMESTAMPTZ DEFAULT now(),
    updated_at    TIMESTAMPTZ DEFAULT now()
);

-- =====================
-- 4. ACCOUNT_MOVEMENTS
-- =====================
CREATE TABLE IF NOT EXISTS account_movements (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_id   UUID NOT NULL REFERENCES operations(id),
    type           VARCHAR(10) NOT NULL CHECK (type IN ('entrada', 'salida')),
    payment_type   VARCHAR(50),
    amount         BIGINT NOT NULL,
    currency       VARCHAR(10) DEFAULT 'USD',
    date           DATE NOT NULL,
    description    TEXT,
    metadata       JSONB,
    attachment_url TEXT,
    created_by     UUID,
    created_at     TIMESTAMPTZ DEFAULT now()
);

-- =====================
-- 5. AUDIT_LOG
-- =====================
CREATE TABLE IF NOT EXISTS audit_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID,
    action     VARCHAR(100) NOT NULL,
    entity     VARCHAR(100) NOT NULL,
    entity_id  UUID,
    payload    JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);
