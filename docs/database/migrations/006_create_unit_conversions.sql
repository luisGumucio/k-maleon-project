CREATE TABLE unit_conversions (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      UUID NOT NULL REFERENCES items(id),
  from_unit_id UUID NOT NULL REFERENCES units(id),
  to_unit_id   UUID NOT NULL REFERENCES units(id),
  factor       NUMERIC NOT NULL,
  UNIQUE (item_id, from_unit_id)
);
