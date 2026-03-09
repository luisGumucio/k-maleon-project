CREATE TABLE shipment_items (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  shipment_id  UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  description  TEXT NOT NULL,
  quantity     INTEGER,
  unit_price   NUMERIC(12, 2),
  amount       NUMERIC(12, 2) NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
