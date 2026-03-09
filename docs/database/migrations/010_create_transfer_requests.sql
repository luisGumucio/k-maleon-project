CREATE TABLE transfer_requests (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id       UUID NOT NULL REFERENCES items(id),
  unit_id       UUID NOT NULL REFERENCES units(id),
  quantity      NUMERIC NOT NULL,
  quantity_base NUMERIC NOT NULL,
  location_id   UUID NOT NULL REFERENCES locations(id),
  status        TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'rejected')),
  notes         TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
