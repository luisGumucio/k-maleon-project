-- Migration 016: Tabla de depósitos de saldo de cuenta
-- Los depósitos son fondeos manuales de la cuenta, sin operación asociada.

CREATE TABLE account_deposits (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES accounts(id),
    amount      BIGINT NOT NULL CHECK (amount > 0),
    description TEXT,
    date        DATE NOT NULL,
    created_by  UUID REFERENCES auth.users(id),
    created_at  TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_account_deposits_account_id ON account_deposits(account_id);
CREATE INDEX idx_account_deposits_created_at ON account_deposits(created_at DESC);
