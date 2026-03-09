-- Agrega email y teléfono a la tabla suppliers
ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS email   VARCHAR(200),
    ADD COLUMN IF NOT EXISTS phone   VARCHAR(50);
