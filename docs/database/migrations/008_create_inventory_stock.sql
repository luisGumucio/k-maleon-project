CREATE TABLE inventory_stock (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      UUID NOT NULL REFERENCES items(id),
  location_id  UUID NOT NULL REFERENCES locations(id),
  quantity     NUMERIC NOT NULL DEFAULT 0,
  min_quantity NUMERIC NOT NULL DEFAULT 0,
  UNIQUE (item_id, location_id)
);
