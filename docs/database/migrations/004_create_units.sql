CREATE TABLE units (
  id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name   TEXT NOT NULL,
  symbol TEXT NOT NULL
);
