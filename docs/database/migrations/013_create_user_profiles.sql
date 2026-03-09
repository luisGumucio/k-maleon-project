-- Perfiles de usuario con roles del sistema
-- El id referencia auth.users de Supabase
CREATE TABLE IF NOT EXISTS user_profiles (
    id          UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    role        TEXT NOT NULL CHECK (role IN ('super_admin', 'admin', 'inventory_admin', 'almacenero', 'encargado_sucursal')),
    name        TEXT,
    location_id UUID REFERENCES locations(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
