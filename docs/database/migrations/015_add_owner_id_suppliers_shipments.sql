-- Agrega owner_id a suppliers y shipments para aislar datos por usuario admin

ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES auth.users(id);

ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES auth.users(id);

CREATE INDEX IF NOT EXISTS idx_suppliers_owner_id ON suppliers(owner_id);
CREATE INDEX IF NOT EXISTS idx_shipments_owner_id ON shipments(owner_id);
