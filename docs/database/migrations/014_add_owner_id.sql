-- Agrega owner_id a accounts y operations para aislar datos por usuario admin

ALTER TABLE accounts
    ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES auth.users(id);

ALTER TABLE operations
    ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES auth.users(id);

-- Índices para queries por owner
CREATE INDEX IF NOT EXISTS idx_accounts_owner_id ON accounts(owner_id);
CREATE INDEX IF NOT EXISTS idx_operations_owner_id ON operations(owner_id);
