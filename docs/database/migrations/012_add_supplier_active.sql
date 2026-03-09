-- Agrega campo active para desactivar proveedores sin borrarlos
ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
