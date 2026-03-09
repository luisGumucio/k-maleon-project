# Feature: Bodega Central e Inventario — K-Maleon

## Contexto

K-Maleon maneja mercadería importada que llega a una bodega central y se
distribuye a sucursales. Este feature digitaliza ese control. La lógica es
idéntica al sistema de bodega de Pippo Pizza pero adaptada al dominio de
importaciones, y agrega el concepto de unidades y conversiones para poder
comprar en unidades grandes (caja, kilo, pallet) y transferir en unidades
pequeñas (unidad, vaso, gramo).

El stock siempre se guarda internamente en la **unidad base** del producto.
La conversión se hace automáticamente antes de registrar cualquier movimiento.

---

## Base de datos

Todo el código y nombres de tablas/columnas en inglés. Solo la UI en español.

### Tabla `units`
Define todas las unidades disponibles en el sistema.

```sql
CREATE TABLE units (
  id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name   TEXT NOT NULL,   -- ej: "kilo", "caja", "unidad", "vaso"
  symbol TEXT NOT NULL    -- ej: "kg", "caja", "und", "vaso"
);
```

### Tabla `items`
Define los productos/insumos del inventario.

```sql
CREATE TABLE items (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name         TEXT NOT NULL,
  base_unit_id UUID NOT NULL REFERENCES units(id),
  active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Tabla `unit_conversions`
Define cuántas unidades base equivale cada unidad de compra/transferencia,
por producto. Se configura una sola vez por producto.

```sql
CREATE TABLE unit_conversions (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      UUID NOT NULL REFERENCES items(id),
  from_unit_id UUID NOT NULL REFERENCES units(id),  -- unidad ingresada por el usuario
  to_unit_id   UUID NOT NULL REFERENCES units(id),  -- unidad base del item
  factor       NUMERIC NOT NULL                      -- cuántas unidades base equivale 1 from_unit
);
```

Ejemplo de datos:

| item    | from_unit  | to_unit | factor |
|---------|------------|---------|--------|
| Huevo   | mapple     | unidad  | 30     |
| Arroz   | kilo       | gramo   | 1000   |
| Arroz   | vaso       | gramo   | 200    |
| Charque | kilo       | gramo   | 1000   |
| Charque | vaso       | gramo   | 80     |

### Tabla `locations`
Define todos los lugares donde existe inventario (bodega y sucursales).

```sql
CREATE TABLE locations (
  id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name   TEXT NOT NULL,
  type   TEXT NOT NULL CHECK (type IN ('warehouse', 'branch')),
  active BOOLEAN NOT NULL DEFAULT TRUE
);
```

Ejemplo de datos:

| name            | type      |
|-----------------|-----------|
| Bodega Central  | warehouse |
| Sucursal Centro | branch    |
| Sucursal Norte  | branch    |

### Tabla `inventory_stock`
Stock actual por item y por ubicación. Se actualiza con cada movimiento.

```sql
CREATE TABLE inventory_stock (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      UUID NOT NULL REFERENCES items(id),
  location_id  UUID NOT NULL REFERENCES locations(id),
  quantity     NUMERIC NOT NULL DEFAULT 0,  -- siempre en unidad base
  min_quantity NUMERIC NOT NULL DEFAULT 0,  -- alerta de stock bajo
  UNIQUE (item_id, location_id)
);
```

### Tabla `inventory_movements`
Historial completo de todos los movimientos. Nunca se elimina.

```sql
CREATE TABLE inventory_movements (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id         UUID NOT NULL REFERENCES items(id),
  unit_id         UUID NOT NULL REFERENCES units(id),  -- unidad ingresada por el usuario
  quantity        NUMERIC NOT NULL,                    -- cantidad ingresada por el usuario
  quantity_base   NUMERIC NOT NULL,                    -- cantidad convertida a unidad base
  movement_type   TEXT NOT NULL CHECK (movement_type IN ('purchase', 'transfer', 'adjustment', 'consumption')),
  location_from   UUID REFERENCES locations(id),       -- null en compras
  location_to     UUID REFERENCES locations(id),       -- null en ajustes negativos
  notes           TEXT,
  created_by      UUID REFERENCES user_profiles(id),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Tabla `transfer_requests`
Solicitudes de transferencia iniciadas por un `encargado_sucursal`. El almacenero
las atiende manualmente.

```sql
CREATE TABLE transfer_requests (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  item_id      UUID NOT NULL REFERENCES items(id),
  unit_id      UUID NOT NULL REFERENCES units(id),
  quantity     NUMERIC NOT NULL,
  quantity_base NUMERIC NOT NULL,
  location_id  UUID NOT NULL REFERENCES locations(id),  -- sucursal solicitante
  status       TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'rejected')),
  notes        TEXT,
  requested_by UUID REFERENCES user_profiles(id),
  resolved_by  UUID REFERENCES user_profiles(id),
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

Antes de registrar cualquier movimiento el backend debe:

1. Buscar en `unit_conversions` el registro donde `item_id` y `from_unit_id`
   coincidan con el item y la unidad ingresada por el usuario.
2. Si no existe conversión → retornar error. No se puede registrar el movimiento.
3. Si la unidad ingresada ya es la unidad base del item → `quantity_base = quantity`
   (factor = 1, no se necesita conversión).
4. `quantity_base = quantity × factor`

Ejemplo:

```
Usuario ingresa: 2 vasos de arroz
Conversión: 1 vaso = 200 gramos
quantity_base = 2 × 200 = 400 gramos
```

La cantidad puede ser decimal (ej: 0.5 vaso = 100 gramos).

---

## Backend — Spring Boot

Seguir los mismos patrones, estructura de paquetes y convenciones del proyecto.

### Endpoints

**Items**
- `GET /api/items` — listar items (filtro `?active=true` para formularios)
- `POST /api/items` — crear item (incluye `base_unit_id`)
- `PUT /api/items/{id}` — editar item
- `DELETE /api/items/{id}` — soft delete (marcar `active = false`)

**Units**
- `GET /api/units` — listar todas las unidades
- `POST /api/units` — crear unidad
- `DELETE /api/units/{id}` — eliminar (solo si no tiene conversiones asociadas)

**Unit Conversions**
- `GET /api/items/{itemId}/conversions` — listar conversiones de un item
- `POST /api/items/{itemId}/conversions` — agregar conversión
- `DELETE /api/items/{itemId}/conversions/{id}` — eliminar conversión

**Locations**
- `GET /api/locations` — listar ubicaciones (filtro `?type=branch` o `?type=warehouse`)
- `POST /api/locations` — crear ubicación
- `PUT /api/locations/{id}` — editar
- `DELETE /api/locations/{id}` — soft delete

**Inventory Stock**
- `GET /api/inventory/stock` — stock actual de todas las ubicaciones por item.
  Respuesta agrupa por item mostrando cantidad en cada location.
  Incluir flag `low_stock: true` cuando `quantity < min_quantity` en alguna
  ubicación.

**Inventory Movements**
- `POST /api/inventory/purchase` — registrar compra en bodega.
  Recibe: `itemId`, `quantity`, `unitId`, `notes`.
  El backend convierte a unidad base, suma a `inventory_stock` de la bodega,
  registra en `inventory_movements`.
- `POST /api/inventory/transfer` — transferir de bodega a sucursal.
  Recibe: `itemId`, `quantity`, `unitId`, `locationToId`, `notes`.
  El backend convierte, valida que haya stock suficiente en bodega,
  descuenta de `inventory_stock` de bodega, suma en `inventory_stock` de
  la sucursal destino, registra en `inventory_movements`. Todo en una
  transacción atómica.
- `POST /api/inventory/consumption` — registrar consumo en sucursal.
  Recibe: `itemId`, `quantity`, `unitId`, `notes`.
  Solo accesible para `encargado_sucursal`. El backend usa la sucursal asignada
  al usuario autenticado (`user_profiles.location_id`), no se envía en el body.
  Convierte a unidad base, descuenta de `inventory_stock` de esa sucursal,
  registra en `inventory_movements` con `movement_type = 'consumption'`.
  Validar que haya stock suficiente antes de descontar.
- `POST /api/inventory/transfer-request` — solicitar transferencia desde sucursal.
  Recibe: `itemId`, `quantity`, `unitId`, `notes`.
  Solo accesible para `encargado_sucursal`. Registra la solicitud (tabla
  `transfer_requests`) con estado `pending`. El `almacenero` o `inventory_admin`
  la verá pendiente y la ejecutará manualmente desde su panel.
- `POST /api/inventory/adjustment` — ajuste manual de stock.
  Recibe: `itemId`, `locationId`, `quantity` (puede ser negativo), `unitId`, `notes`.
  Solo accesible para `inventory_admin` y `almacenero`.
- `GET /api/inventory/movements` — historial con filtros opcionales:
  `?type=`, `?itemId=`, `?locationId=`, `?from=`, `?to=`.
  `encargado_sucursal` solo puede ver movimientos de su propia sucursal.
- `GET /api/inventory/stock` — stock actual.
  `encargado_sucursal` solo recibe el stock de su sucursal asignada.
  `almacenero` e `inventory_admin` ven todas las ubicaciones.

### Validaciones

- No transferir más stock del disponible en bodega → error con cantidad actual disponible.
- No consumir más stock del disponible en la sucursal → error con cantidad actual.
- No registrar compra con quantity ≤ 0.
- No transferir a una location inactiva.
- No usar items inactivos en formularios de movimientos.
- Si no existe conversión para el item + unidad ingresada → error descriptivo.
- `encargado_sucursal` solo puede operar sobre su propia sucursal asignada.
  Si intenta acceder a datos de otra sucursal → 403.

---

## Frontend — Refine + Ant Design

Seguir los mismos patrones del proyecto. Estas vistas pertenecen al layout de
`inventory_admin`.

### Vista principal — Stock (`/stock`)

Tabla con columnas: **Item | Bodega Central | Sucursal A | Sucursal B | Total**
(las columnas de ubicación se generan dinámicamente según las locations activas).

- Ícono de alerta ⚠️ en la celda cuando `quantity < min_quantity` en esa ubicación.
- Botón "Transferir" por fila → navega a `/inventory/transfer?itemId=...`
- Botón "Nueva compra" en el header → navega a `/inventory/purchase`

### Vista de compra (`/inventory/purchase`)

Formulario:
- Item (select, solo activos)
- Cantidad (número, permite decimales)
- Unidad (select, carga las conversiones disponibles del item seleccionado +
  la unidad base como opción siempre presente)
- Notas (opcional)
- Al seleccionar item + unidad → mostrar "Equivale a X [unidad base]" en tiempo real
- Confirmar → llama `POST /api/inventory/purchase`

### Vista de transferencia (`/inventory/transfer`)

Formulario:
- Item (select, solo activos; pre-seleccionado si viene `?itemId=` en la URL)
- Cantidad (número, permite decimales)
- Unidad (select igual que en compra)
- Sucursal destino (select de locations tipo `branch` activas)
- Mostrar "Disponible en bodega: X [unidad base]" al seleccionar el item
- Mostrar "Equivale a X [unidad base]" al ingresar cantidad + unidad
- Error inline si stock insuficiente
- Confirmar → llama `POST /api/inventory/transfer`

### Vista de ajuste (`/inventory/adjustment`)

Formulario:
- Item (select, solo activos)
- Ubicación (select de todas las locations activas)
- Cantidad (número, permite negativos para correcciones a la baja)
- Unidad
- Notas (obligatorio en ajustes)
- Confirmar → llama `POST /api/inventory/adjustment`

### Vista de historial (`/inventory/movements`)

Tabla: **Fecha | Tipo | Item | Cantidad | Unidad | Equivale (base) | Origen | Destino | Notas**

Filtros: tipo de movimiento, item, ubicación, fecha desde/hasta.

### Configuración — Items (`/items`)

CRUD de productos/insumos. Al crear o editar un item, dentro del mismo formulario
o en una sección expandible, gestionar las conversiones de unidades (agregar,
editar, eliminar conversiones para ese item).

### Configuración — Unidades (`/units`)

CRUD simple de unidades. Tabla con nombre y símbolo.

### Configuración — Ubicaciones (`/locations`)

CRUD de bodegas y sucursales. Columnas: nombre, tipo, activo.

---

## Vistas del Encargado de Sucursal

El `encargado_sucursal` tiene su propio layout reducido con acceso únicamente
a los datos de su sucursal asignada.

### Menú lateral

- **Mi Stock** → `/my/stock`
- **Registrar Consumo** → `/my/consumption`
- **Solicitar Transferencia** → `/my/request`
- **Historial** → `/my/movements`

### Mi Stock (`/my/stock`)

Tabla simple: **Producto | Disponible | Unidad base | Alerta**
Solo muestra el stock de su sucursal. Alerta ⚠️ cuando `quantity < min_quantity`.

### Registrar Consumo (`/my/consumption`)

Formulario:
- Producto (select, solo activos con stock > 0 en su sucursal)
- Cantidad (número, permite decimales)
- Unidad (conversiones disponibles del producto)
- Mostrar "Disponible: X [unidad base]" al seleccionar producto
- Notas (opcional)
- Confirmar → llama `POST /api/inventory/consumption`
- Error inline si stock insuficiente

### Solicitar Transferencia (`/my/request`)

Formulario:
- Producto (select, solo activos)
- Cantidad
- Unidad
- Notas (opcional)
- Confirmar → llama `POST /api/inventory/transfer-request`
- Muestra listado de solicitudes anteriores con su estado (pendiente, completada,
  rechazada)

### Historial (`/my/movements`)

Tabla: **Fecha | Tipo | Producto | Cantidad | Unidad | Notas**
Solo movimientos de su sucursal. Sin filtro de ubicación (ya está fijo a la suya).

---

## Vista de solicitudes pendientes (inventory_admin y almacenero)

Agregar una sección **Solicitudes** (`/inventory/requests`) accesible para
`inventory_admin` y `almacenero` donde ven todas las solicitudes con estado
`pending`. Por cada solicitud pueden:

- **Completar** → ejecuta el transfer directamente desde ahí
- **Rechazar** → cambia estado a `rejected` con nota opcional

```
BD (tablas) → Units + Locations CRUD → Items CRUD → Unit Conversions →
Inventory Stock endpoint → Purchase → Transfer → Adjustment → Historial →
Vista principal de stock con alertas
```

---

## Notas importantes

- Todo el código, tablas y columnas en inglés. Solo la UI en español.
- Las ubicaciones son dinámicas, no hardcodeadas. La vista de stock genera
  columnas según las locations activas.
- El stock siempre se persiste en unidad base. Nunca almacenar en la unidad
  ingresada por el usuario.
- La conversión de unidades ocurre en el backend, no en el frontend. El frontend
  solo muestra el equivalente calculado como ayuda visual.
- Todos los endpoints de inventario solo accesibles para `inventory_admin` y
  `super_admin`.