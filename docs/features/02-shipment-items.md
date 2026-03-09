# Feature: Detalle de Contenedor (Shipment Items)

## Contexto

Cada contenedor registrado en `shipments` puede contener múltiples productos. Este feature permite registrar el detalle de mercadería por contenedor: qué productos vienen, en qué cantidad, a qué precio unitario y con qué importe (todos ingresados manualmente).

El usuario selecciona un contenedor existente del listado de `shipments` y gestiona sus ítems desde una vista dedicada en el menú.

---

## Reglas de negocio

- El **importe** es un campo libre — el usuario lo ingresa directamente. No se calcula automáticamente.
- El **total del contenedor** es la única suma automática: `SUM(importe)` de todos los ítems.
- Un ítem puede tener cantidad y precio unitario como referencia, pero el importe no depende de ellos.
- Se pueden agregar, editar y eliminar ítems individualmente.
- El contenedor en sí (fechas, proveedor, etc.) **no se edita desde esta vista** — solo se selecciona.

---

## Base de Datos

### Nueva tabla: `shipment_items`

```sql
CREATE TABLE shipment_items (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  shipment_id      UUID NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
  description      TEXT NOT NULL,
  quantity         INTEGER,
  unit_price       NUMERIC(12, 2),
  amount           NUMERIC(12, 2) NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

> `amount` es ingresado manualmente por el usuario. `quantity` y `unit_price` son opcionales, solo de referencia.
> `ON DELETE CASCADE` — al eliminar un shipment se eliminan sus ítems automáticamente.

### Migration

Crear `docs/database/migrations/003_create_shipment_items.sql`

---

## Backend — Spring Boot

Seguir los mismos patrones, estructura de paquetes y convenciones del proyecto.

### Modelo: `ShipmentItem.java`

Campos: `id`, `shipment` (ManyToOne lazy a `Shipment`), `description`, `quantity`, `unitPrice`, `amount`, `createdAt`, `updatedAt`.

### DTOs

**`ShipmentItemRequest`**
- `shipmentId` (UUID, `@NotNull`)
- `description` (String, `@NotBlank`)
- `quantity` (Integer, opcional)
- `unitPrice` (BigDecimal, opcional)
- `amount` (BigDecimal, `@NotNull`, `@Positive`)

**`ShipmentItemResponse`**
- Todos los campos del ítem más `shipmentId` y `containerNumber` (para contexto).

**`ShipmentDetailResponse`** *(nuevo, para la vista de detalle)*
- Datos del shipment: `id`, `number`, `containerNumber`, `supplierName`, `departureDate`, `arrivalDate`
- Lista de ítems: `List<ShipmentItemResponse>`
- `totalAmount`: `SUM(amount)` de todos los ítems

### Endpoints

```
GET    /api/shipment-items?shipmentId={id}    → ítems de un contenedor + total
POST   /api/shipment-items                   → agregar ítem
PUT    /api/shipment-items/{id}              → editar ítem
DELETE /api/shipment-items/{id}              → eliminar ítem
```

> No hay endpoint de listado general — siempre se filtra por `shipmentId`.

### Servicio: `ShipmentItemService`

- `findByShipmentId(UUID shipmentId)` → valida que el shipment exista, retorna ítems + total calculado
- `create(ShipmentItemRequest)` → valida shipment, inserta ítem
- `update(UUID id, ShipmentItemRequest)` → actualiza ítem o `ResourceNotFoundException`
- `delete(UUID id)` → elimina ítem o `ResourceNotFoundException`

---

## Frontend — Refine + Ant Design

### Nueva sección en el menú: "Contenidos"

Recurso independiente: `shipment-items`. Aparece como ítem propio en el sidebar.

### Vista principal (`ShipmentItemList`)

1. **Selector de contenedor** en la parte superior:
   - `Select` con búsqueda que carga de `/api/shipments`
   - Muestra: `#N° — CARU5170029 (Proveedor)`
   - Al seleccionar, carga los ítems de ese contenedor

2. **Resumen del contenedor** (cuando hay uno seleccionado):
   ```
   Contenedor: CARU5170029 | Proveedor: China Trading | Partida: 08/03/2026
   Total: $1,180.00
   ```

3. **Tabla de ítems**:

   | Descripción | Cantidad | Precio Unit. | Importe | Acciones |
   |---|---|---|---|---|
   | Converse mujer | 100 | $5.00 | $500.00 | ✏️ 🗑️ |
   | Asia mujer | 80 | $6.00 | $480.00 | ✏️ 🗑️ |

4. **Botón "Agregar ítem"** — abre modal inline para ingresar descripción, cantidad (opcional), precio unitario (opcional), importe (requerido).

### Formulario de ítem (modal)

Campos:
- Descripción (texto, requerido)
- Cantidad (número entero, opcional)
- Precio unitario (decimal, opcional)
- Importe (decimal, requerido)

### Responsive

- Desktop: tabla con columnas completas
- Mobile: cards con descripción, importe destacado, acciones

---

## Notas importantes

- El selector de contenedor **no usa `useTable` de Refine** — se carga con `useSelect` o `useQuery` directo a `/api/shipments`.
- El total se recalcula en el backend en cada respuesta — el frontend nunca suma importes.
- Los campos `quantity` y `unit_price` son puramente informativos — el usuario puede dejarlos vacíos.
- Al eliminar un shipment desde la vista de Rastreo, sus ítems se eliminan automáticamente por el `ON DELETE CASCADE`.
