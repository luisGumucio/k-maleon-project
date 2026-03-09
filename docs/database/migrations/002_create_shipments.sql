CREATE TABLE shipments (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  number           INTEGER GENERATED ALWAYS AS IDENTITY,
  supplier_id      UUID NOT NULL REFERENCES suppliers(id),
  departure_date   DATE,
  container_number TEXT,
  quantity         INTEGER,
  product_details  TEXT,
  arrival_date     DATE,
  document_url     TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
