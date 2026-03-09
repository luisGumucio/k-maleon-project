# Plan: Feature 03 — Layouts de UI por Rol

## Estado: IMPLEMENTADO

---

## Fases completadas

### Fase 1 — Configuración base de roles ✅
- `VITE_MOCK_ROLE` agregado a `.env.example`
- `src/contexts/role/index.tsx` — `RoleContext` con `role`, `viewingAs`, `setViewingAs`
- `App.tsx` refactorizado: lee el rol y monta el layout correspondiente

### Fase 2 — Layout Admin ✅
- `src/components/layouts/AdminLayout.tsx` — layout con todos los recursos Admin
- `src/pages/admin/dashboard.tsx` — saldo de cuenta + operaciones activas (consume API)
- Prop `fromSuperAdmin` muestra botón "Volver al Dashboard" en el sider

### Fase 3 — Layout InventoryAdmin ✅
- `src/components/layouts/InventoryLayout.tsx`
- `src/pages/inventory/dashboard.tsx` — stats mock (stock total, sucursales activas)
- `src/pages/warehouses/list.tsx` — CRUD local (datos mock)
- `src/pages/branches/list.tsx` — CRUD local (datos mock)
- `src/pages/stock/list.tsx` — tabla solo lectura con filtro (datos mock)
- `src/pages/inventory-users/list.tsx` — CRUD, rol fijo `almacenero`

### Fase 4 — Layout SuperAdmin ✅
- `src/components/layouts/SuperAdminLayout.tsx`
- `src/pages/super/dashboard.tsx` — dos tarjetas para navegar a paneles hijos
- `src/pages/users/list.tsx` — CRUD usuarios, todos los roles seleccionables
- `src/pages/settings/index.tsx` — placeholder "Configuraciones — próximamente"

### Fase 5 — Navegación SuperAdmin → paneles hijos ✅
- `viewingAs` en `RoleContext` controla qué layout se monta
- `setViewingAs("admin")` / `setViewingAs("inventory_admin")` desde el dashboard de super_admin
- `setViewingAs(null)` en el botón "Volver al Dashboard" dentro del sider del layout hijo

---

## Archivos creados/modificados

| Archivo | Tipo |
|---|---|
| `frontend/.env.example` | Modificado |
| `frontend/src/App.tsx` | Modificado |
| `frontend/src/contexts/role/index.tsx` | Nuevo |
| `frontend/src/components/layouts/AdminLayout.tsx` | Nuevo |
| `frontend/src/components/layouts/InventoryLayout.tsx` | Nuevo |
| `frontend/src/components/layouts/SuperAdminLayout.tsx` | Nuevo |
| `frontend/src/pages/admin/dashboard.tsx` | Nuevo |
| `frontend/src/pages/inventory/dashboard.tsx` | Nuevo |
| `frontend/src/pages/warehouses/list.tsx` | Nuevo |
| `frontend/src/pages/branches/list.tsx` | Nuevo |
| `frontend/src/pages/stock/list.tsx` | Nuevo |
| `frontend/src/pages/inventory-users/list.tsx` | Nuevo |
| `frontend/src/pages/super/dashboard.tsx` | Nuevo |
| `frontend/src/pages/users/list.tsx` | Nuevo |
| `frontend/src/pages/settings/index.tsx` | Nuevo |

---

## Notas pendientes

- Las páginas de InventoryAdmin usan estado local (no API) — conectar cuando el backend del módulo de inventario esté listo
- La página de Usuarios (`/users` y `/inventory/users`) es mock — conectar al endpoint real cuando exista
- Al implementar auth real: eliminar `VITE_MOCK_ROLE` y reemplazar por `authProvider.getPermissions()`
- El layout de `almacenero` se implementa en una iteración posterior
