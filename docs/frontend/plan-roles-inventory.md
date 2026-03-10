# Plan Frontend: Roles de Inventario — almacenero y encargado_sucursal

## Estado: COMPLETADO

## Contexto

El sistema define 5 roles (`super_admin`, `admin`, `inventory_admin`, `almacenero`, `encargado_sucursal`).
El frontend actual tiene `"branch_manager"` como nombre de rol en lugar de `"encargado_sucursal"`,
y no tiene soporte para `"almacenero"`. Hay que corregir los nombres y crear el layout del almacenero.

---

## Diagnóstico del estado actual

| Archivo | Problema |
|---|---|
| `src/contexts/role/index.tsx` | `AppRole` tiene `"branch_manager"` — debe ser `"encargado_sucursal"`. Falta `"almacenero"`. |
| `src/App.tsx` | Solo rutea `"branch_manager"` → `BranchLayout`. Falta `"almacenero"` → `AlmaceneroLayout`. |
| `src/components/layouts/BranchLayout.tsx` | Funciona bien, solo hay que asegurarse que responde a `"encargado_sucursal"`. |
| `src/pages/branch/*` | Usan `VITE_MOCK_BRANCH_LOCATION_ID`. Funciona igual con el nuevo nombre de rol. |

---

## Fases

### F1 — Corregir nombres de rol ✅

**`src/contexts/role/index.tsx`**
- Renombrar `"branch_manager"` → `"encargado_sucursal"` en `AppRole` y en `VALID_ROLES`.
- Agregar `"almacenero"` a `AppRole` y a `VALID_ROLES`.

**`src/App.tsx`**
- Cambiar la condición `role === "branch_manager"` → `role === "encargado_sucursal"`.
- Agregar `if (role === "almacenero") return <AlmaceneroLayout />;`.
- Importar `AlmaceneroLayout`.

---

### F2 — Crear AlmaceneroLayout ✅

**`src/components/layouts/AlmaceneroLayout.tsx`**

Menú del almacenero (opera en bodega central):
- Stock Bodega → `/almacen/stock`
- Registrar Compra → `/almacen/purchase`
- Transferir a Sucursal → `/almacen/transfer`
- Solicitudes Pendientes → `/almacen/requests`
- Historial → `/almacen/movements`

Mismo patrón que `BranchLayout` (Refine + ThemedLayout + ThemedSider).

---

### F3 — Crear páginas del almacenero ✅

Todas en `src/pages/almacen/`.

**`stock.tsx`** — `/almacen/stock`
- Igual que `src/pages/stock/list.tsx` pero filtrado solo a ubicaciones `type=warehouse`.
- Botón "Transferir" por fila → `/almacen/transfer?itemId=...`.
- Botón "Registrar Compra" en header → `/almacen/purchase`.

**`purchase.tsx`** — `/almacen/purchase`
- Reusar lógica de `src/pages/inventory/purchase.tsx` tal cual.
- El almacenero solo puede seleccionar destino `warehouse`.

**`transfer.tsx`** — `/almacen/transfer`
- Reusar lógica de `src/pages/inventory/transfer.tsx` tal cual.
- El origen siempre es el warehouse (sin select de origen).
- El destino son solo `branches`.
- Pre-selecciona `?itemId=` si viene en URL.

**`requests.tsx`** — `/almacen/requests`
- Tabla de solicitudes pendientes (`status=pending`).
- Botón "Completar" → `POST /api/inventory/transfer-requests/{id}/complete`.
- Botón "Rechazar" → modal con nota → `POST /api/inventory/transfer-requests/{id}/reject`.
- Solo muestra `pending` por defecto (mismo que `src/pages/inventory/requests.tsx`).

**`movements.tsx`** — `/almacen/movements`
- Igual que `src/pages/inventory/movements.tsx`.
- Sin filtro de ubicación fija (ve todo).

---

### F4 — Actualizar SuperAdminLayout para navegar al panel almacenero ✅

**`src/components/layouts/SuperAdminLayout.tsx`**
- Agregar opción "Ver como Almacenero" en el selector de `viewingAs` si ya existe ese patrón.
- Agregar condición en `App.tsx`: `if (role === "super_admin" && viewingAs === "almacenero") return <AlmaceneroLayout fromSuperAdmin />;`.

---

## Archivos a crear

- `src/components/layouts/AlmaceneroLayout.tsx`
- `src/pages/almacen/stock.tsx`
- `src/pages/almacen/purchase.tsx`
- `src/pages/almacen/transfer.tsx`
- `src/pages/almacen/requests.tsx`
- `src/pages/almacen/movements.tsx`

## Archivos a modificar

- `src/contexts/role/index.tsx`
- `src/App.tsx`
- `src/components/layouts/SuperAdminLayout.tsx` (F4)

---

## Notas

- Las páginas del almacenero reusan lógica de las páginas de `inventory_admin`.
  No duplicar lógica — si la página es idéntica, importar el mismo componente.
- `VITE_MOCK_BRANCH_LOCATION_ID` sigue siendo válido para el `encargado_sucursal` hasta
  que el backend envíe el `location_id` en el token/perfil.
- El almacenero no tiene `location_id` fijo — opera sobre todos los warehouses.
