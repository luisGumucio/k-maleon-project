CREATE TABLE inventory_movements (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id       UUID NOT NULL REFERENCES items(id),
  unit_id       UUID NOT NULL REFERENCES units(id),
  quantity      NUMERIC NOT NULL,
  quantity_base NUMERIC NOT NULL,
  movement_type TEXT NOT NULL CHECK (movement_type IN ('purchase', 'transfer', 'adjustment', 'consumption')),
  location_from UUID REFERENCES locations(id),
  location_to   UUID REFERENCES locations(id),
  notes         TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
