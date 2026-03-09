CREATE TABLE locations (
  id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name   TEXT NOT NULL,
  type   TEXT NOT NULL CHECK (type IN ('warehouse', 'branch')),
  active BOOLEAN NOT NULL DEFAULT TRUE
);
