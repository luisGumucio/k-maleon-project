# Frontend Plan — Feature: Shipment Items (Contenidos)

> Validar cada fase antes de avanzar a la siguiente.
> Prerequisito: Backend Shipment Items Fases 1–6 completadas.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Tipos y registro del recurso

**Objetivo:** Definir los tipos y registrar el recurso en Refine.

### Tasks
- [ ] Crear `src/types/shipment-item.ts`
  ```ts
  export type ShipmentItem = {
    id: string;
    shipmentId: string;
    containerNumber: string;
    description: string;
    quantity: number | null;
    unitPrice: number | null;
    amount: number;
    createdAt: string;
    updatedAt: string;
  };

  export type ShipmentDetail = {
    id: string;
    number: number;
    containerNumber: string;
    supplierName: string;
    departureDate: string | null;
    arrivalDate: string | null;
    items: ShipmentItem[];
    totalAmount: number;
  };
  ```
- [ ] Registrar recurso `shipment-items` en `App.tsx`
  - Solo `list` (sin show/create/edit — todo se maneja en la misma vista)
  - `meta: { label: "Contenidos" }`
- [ ] Agregar ruta `/shipment-items` en `App.tsx`

### Validación
- [ ] El ítem "Contenidos" aparece en el sidebar

---

## FASE 2 — Vista principal (`ShipmentItemList`)

**Objetivo:** Página con selector de contenedor, resumen y tabla de ítems.

### Tasks
- [ ] Crear `src/pages/shipment-items/list.tsx`

  **Selector de contenedor:**
  - `useQuery` a `GET /api/shipments` para cargar todos los shipments
  - `Select` con búsqueda — opción muestra: `#N° — CARU5170029 (Proveedor)`
  - Estado local `selectedShipmentId` — al cambiar, carga el detalle

  **Carga del detalle:**
  - `useQuery` a `GET /api/shipment-items?shipmentId={id}` — solo ejecuta cuando hay selección
  - Retorna `ShipmentDetail` con ítems y `totalAmount`

  **Resumen del contenedor** (visible solo cuando hay selección):
  - Fila con: N° contenedor, proveedor, fecha partida, fecha llegada
  - `Statistic` con total: `$1,180.00`

  **Tabla de ítems** (desktop):
  | Descripción | Cantidad | Precio Unit. | Importe | Acciones |
  |---|---|---|---|---|
  - Acciones: botón editar (abre modal de edición), botón eliminar (con confirmación)
  - Fila de total al pie de la tabla

  **Cards de ítems** (mobile):
  - Descripción en negrita, importe destacado, acciones en la esquina

  **Botón "Agregar ítem":**
  - Visible solo cuando hay un contenedor seleccionado
  - Abre `ItemFormModal` en modo creación

### Validación
- [ ] Al seleccionar un contenedor se cargan sus ítems
- [ ] El total se muestra correctamente
- [ ] Si no hay ítems, muestra estado vacío con mensaje

---

## FASE 3 — Modal de ítem (`ItemFormModal`)

**Objetivo:** Modal para crear y editar ítems.

### Tasks
- [ ] Crear `src/pages/shipment-items/ItemFormModal.tsx`
  - Props: `open`, `onClose`, `onSuccess`, `shipmentId`, `item?` (si viene = modo edición)
  - Campos:
    - Descripción (`Input`, requerido)
    - Cantidad (`InputNumber`, entero, opcional)
    - Precio unitario (`InputNumber`, 2 decimales, opcional, prefijo `$`)
    - Importe (`InputNumber`, 2 decimales, requerido, prefijo `$`)
  - En modo edición: pre-carga los valores del ítem
  - Al guardar:
    - Modo creación: `POST /api/shipment-items` con `{ shipmentId, ...fields }`
    - Modo edición: `PUT /api/shipment-items/{id}` con `{ shipmentId, ...fields }`
  - Usar `fetchJson` de `src/providers/data.ts`
  - `message.success` / `message.error` según resultado
  - Al éxito: llama `onSuccess()` para que el padre refresque los datos

### Validación
- [ ] Crear ítem → aparece en la tabla
- [ ] Editar ítem → valores pre-cargados, se actualiza correctamente
- [ ] Importe vacío muestra error de validación

---

## FASE 4 — Eliminar ítem

**Objetivo:** Eliminar un ítem con confirmación.

### Tasks
- [ ] En `list.tsx`, botón eliminar usa `Popconfirm` de Ant Design
  - Mensaje: "¿Eliminar este ítem?"
  - Al confirmar: `DELETE /api/shipment-items/{id}` con `fetchJson`
  - Al éxito: refresca la query del detalle con `queryClient.invalidateQueries`
  - `message.success` / `message.error`

### Validación
- [ ] Eliminar ítem → desaparece de la tabla y el total se actualiza

---

## FASE 5 — Índice y registro en App

**Objetivo:** Exportar componentes y conectar en el router.

### Tasks
- [ ] Crear `src/pages/shipment-items/index.ts` exportando `ShipmentItemList`
- [ ] Importar y agregar la ruta en `App.tsx`

### Validación
- [ ] Flujo completo: seleccionar contenedor → agregar ítems → editar → eliminar → total correcto

---

## Notas de implementación

- **No usar `useTable` de Refine** — la carga de ítems es con `useQuery` de `@tanstack/react-query` directo a la API, igual que `MovementTable`
- **`selectedShipmentId`** vive en estado local de `list.tsx` — no se sincroniza con la URL
- El **total** viene del backend en `ShipmentDetail.totalAmount` — el frontend nunca lo calcula
- Los campos `quantity` y `unitPrice` son opcionales — mostrar `—` cuando son `null`

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5
```
