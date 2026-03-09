# Plan Frontend: Feature 05 — Bodega Central e Inventario

## Estado: PENDIENTE

---

## Dependencias

- Backend B1–B6 implementado ✅
- Layout `inventory_admin` ya existe en `src/components/layouts/InventoryLayout.tsx`
- Páginas mock de warehouses/branches/stock a reemplazar

---

## Fases

### F1 — Actualizar InventoryLayout con rutas reales ⬜
Reemplazar rutas mock por las nuevas en `InventoryLayout.tsx`:

**Menú actualizado:**
- Dashboard → `/inventory/dashboard`
- Stock → `/stock`
- Compra → `/inventory/purchase`
- Transferencia → `/inventory/transfer`
- Ajuste → `/inventory/adjustment`
- Historial → `/inventory/movements`
- Solicitudes → `/inventory/requests`
- Items → `/items`
- Unidades → `/units`
- Ubicaciones → `/locations`

**Eliminar del layout:** rutas mock de `/warehouses`, `/branches`, `/inventory/users`

---

### F2 — Configuración CRUD (Items, Unidades, Ubicaciones) ⬜

**`/units`** — `src/pages/units/list.tsx`
- Tabla: Nombre | Símbolo | Acciones
- Modal crear: nombre + símbolo
- Delete con confirmación (error si tiene conversiones → mostrar mensaje del API)

**`/locations`** — `src/pages/locations/list.tsx`
- Tabla: Nombre | Tipo (tag warehouse/branch) | Activo | Acciones
- Modal crear/editar: nombre + tipo (select)
- Delete = soft delete

**`/items`** — `src/pages/items/list.tsx` + `src/pages/items/conversions.tsx`
- Tabla: Nombre | Unidad base | Activo | Acciones
- Modal crear/editar item: nombre + unidad base (select de `/api/units`)
- Sección expandible por fila: tabla de conversiones del item
  - Columnas: Desde | Hacia | Factor | Eliminar
  - Botón agregar conversión → modal: fromUnit + toUnit + factor

---

### F3 — Vista principal de Stock ⬜

**`/stock`** — `src/pages/stock/list.tsx` (reemplaza el mock)
- Fetch: `GET /api/inventory/stock`
- Columnas dinámicas: **Item** + una columna por location activa + **Total**
- Ícono ⚠️ en celda cuando `lowStock: true`
- Botón "Transferir" por fila → `/inventory/transfer?itemId=...`
- Botón "Nueva compra" en header → `/inventory/purchase`
- Mostrar símbolo de unidad base junto a cantidades

---

### F4 — Formularios de movimiento ⬜

**`/inventory/purchase`** — `src/pages/inventory/purchase.tsx`
- Select item (solo activos, `GET /api/items?active=true`)
- Al seleccionar item → carga conversiones `GET /api/items/{id}/conversions`
- Select unidad (conversiones del item + unidad base siempre presente)
- Input cantidad (decimal)
- Preview en tiempo real: "Equivale a X [unidad base]" (cálculo: `quantity × factor`)
- Notas (opcional)
- Submit → `POST /api/inventory/purchase`

**`/inventory/transfer`** — `src/pages/inventory/transfer.tsx`
- Pre-selecciona item si viene `?itemId=` en URL
- Misma lógica de unidades que purchase
- Select sucursal destino (`GET /api/locations?type=branch`)
- Preview "Disponible en bodega: X [unidad base]" al seleccionar item
  - Fetch stock del item y busca la entrada warehouse
- Preview equivalente
- Submit → `POST /api/inventory/transfer`

**`/inventory/adjustment`** — `src/pages/inventory/adjustment.tsx`
- Select item + ubicación (todas las activas) + unidad + cantidad (permite negativo) + notas obligatorio
- Submit → `POST /api/inventory/adjustment`

**`/inventory/movements`** — `src/pages/inventory/movements.tsx`
- Tabla: Fecha | Tipo | Item | Cantidad | Unidad | Equivale (base) | Origen | Destino | Notas
- Filtros: tipo (select), item (select), ubicación (select), fecha desde/hasta (DatePicker range)
- Fetch: `GET /api/inventory/movements` con params

---

### F5 — Solicitudes de transferencia ⬜

**`/inventory/requests`** — `src/pages/inventory/requests.tsx`
- Tabla: Fecha | Item | Cantidad | Unidad | Sucursal | Estado | Acciones
- Solo muestra `pending`
- Botón "Completar" → `POST /api/inventory/requests/{id}/complete`
- Botón "Rechazar" → modal con nota opcional → `POST /api/inventory/requests/{id}/reject`

---

### F6 — Layout encargado de sucursal ⬜

**Nuevo rol** en `src/contexts/role/index.tsx`: agregar `"branch_manager"` a `AppRole`

**Nuevo layout** `src/components/layouts/BranchLayout.tsx`:
- Menú: Mi Stock | Registrar Consumo | Solicitar Transferencia | Historial

**Páginas:**

`/my/stock` — `src/pages/branch/stock.tsx`
- `GET /api/inventory/stock` filtrado por su location
- Tabla: Producto | Disponible | Unidad base | ⚠️ alerta

`/my/consumption` — `src/pages/branch/consumption.tsx`
- Misma lógica de formulario que purchase pero llama `POST /api/inventory/consumption`
- locationId fijo (hardcodeado por ahora, reemplazar con auth)

`/my/request` — `src/pages/branch/request.tsx`
- Formulario → `POST /api/inventory/transfer-request`
- Tabla de solicitudes anteriores del encargado con estado

`/my/movements` — `src/pages/branch/movements.tsx`
- `GET /api/inventory/movements?locationId=...`
- Sin filtro de ubicación (fijo a su sucursal)

---

## Tipos TypeScript a crear

`src/types/inventory.ts`:
```ts
type Unit = { id: string; name: string; symbol: string }
type Item = { id: string; name: string; baseUnitId: string; baseUnitName: string; baseUnitSymbol: string; active: boolean; createdAt: string }
type UnitConversion = { id: string; itemId: string; fromUnitId: string; fromUnitName: string; fromUnitSymbol: string; toUnitId: string; toUnitName: string; toUnitSymbol: string; factor: number }
type Location = { id: string; name: string; type: 'warehouse' | 'branch'; active: boolean }
type StockLocationEntry = { locationId: string; locationName: string; locationType: string; quantity: number; minQuantity: number; lowStock: boolean }
type ItemStock = { itemId: string; itemName: string; baseUnitSymbol: string; locations: StockLocationEntry[]; totalQuantity: number }
type InventoryMovement = { id: string; itemId: string; itemName: string; unitId: string; unitSymbol: string; quantity: number; quantityBase: number; baseUnitSymbol: string; movementType: string; locationFromId: string | null; locationFromName: string | null; locationToId: string | null; locationToName: string | null; notes: string | null; createdAt: string }
type TransferRequestItem = { id: string; itemId: string; itemName: string; unitId: string; unitSymbol: string; quantity: number; quantityBase: number; baseUnitSymbol: string; locationId: string; locationName: string; status: 'pending' | 'completed' | 'rejected'; notes: string | null; createdAt: string; updatedAt: string }
```

---

## Notas importantes

- El cálculo del equivalente en base (preview) se hace en el frontend solo como ayuda visual — el backend siempre recalcula
- Las columnas de stock son dinámicas: generarlas a partir de `locations` en la respuesta del primer item
- Usar `useQuery` de `@tanstack/react-query` para todos los fetches (mismo patrón que `AccountBalance`)
- Usar `useMutation` para todos los POST
- Responsive: Cards en mobile, Table en desktop (mismo patrón del proyecto)
- Al completar cada fase actualizar este archivo
