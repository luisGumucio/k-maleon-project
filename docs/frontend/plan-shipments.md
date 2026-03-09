# Frontend Plan — Feature: Shipments

> Validar cada fase antes de avanzar a la siguiente.
> Prerequisito: Backend Shipments Fases 1–7 completadas.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Tipos y utilidades

**Objetivo:** Definir el tipo `Shipment` y registrar el recurso en Refine.

### Tasks
- [x] Crear `src/types/shipment.ts`
- [x] Registrar recurso `shipments` en `App.tsx` con `list`, `show`, `create`, `edit` y `meta: { label: "Rastreo" }`
- [x] Agregar rutas en `App.tsx` para los 4 componentes

### Validación
- [x] El ítem "Rastreo" aparece en el sidebar

---

## FASE 2 — Lista de envíos (`ShipmentList`)

**Objetivo:** Tabla con filtros y acciones.

### Tasks
- [x] Crear `src/pages/shipments/list.tsx`
  - `useTable<Shipment>` con `syncWithLocation`
  - Columnas: N°, Proveedor, N° Contenedor, Fecha Partida, Fecha Llegada, Cantidad, Acciones
  - Filtros: proveedor (Select), N° Contenedor (Input), rango de fechas de partida (RangePicker, oculto en mobile)
  - Botones Filtrar / Limpiar
  - Layout desktop: todos los filtros en fila horizontal con `flex` + `gap`
  - Layout mobile: filtros apilados verticalmente, sin date picker
  - Acciones: `ShowButton`, `EditButton`, `DeleteButton`
  - Responsive: cards en mobile, tabla en desktop

### Validación
- [x] La lista carga y muestra envíos
- [x] Los filtros quedan en una sola línea horizontal en desktop
- [x] El filtro por proveedor, contenedor y fechas funcionan
- [x] Los botones de acción navegan correctamente

---

## FASE 3 — Formulario de creación y edición (`ShipmentForm`)

**Objetivo:** Formulario compartido para crear y editar envíos.

### Tasks
- [x] Crear `src/pages/shipments/form.tsx` con `useShipmentForm(action)` + `ShipmentFormFields`
  - Fechas formateadas a `YYYY-MM-DD` antes de enviar
  - Pre-carga con `query?.data?.data` en edición
- [x] Crear `src/pages/shipments/create.tsx` — redirige a lista tras crear
- [x] Crear `src/pages/shipments/edit.tsx` — incluye sección de documento (Fase 4)

### Notas
- `useShipmentForm` acepta `documentUrl` opcional — lo inyecta en el payload del `onFinish`
- `create.tsx` maneja el upload antes de guardar (igual que `MovementCreateModal`)

### Validación
- [x] Crear un envío → aparece en la lista con `number` autoincremental
- [x] Crear un envío con documento → `documentUrl` se persiste
- [ ] Editar un envío → los valores se pre-cargan correctamente
- [ ] Validación de campo Proveedor (requerido) funciona

---

## FASE 4 — Upload de documento en edición

**Objetivo:** Subir comprobante y guardar URL en el shipment.

### Tasks
- [x] Sección "Documento" en `edit.tsx`:
  - Si `documentUrl` existe: enlace "Ver documento" + botón "Reemplazar"
  - Si no existe: botón "Subir documento"
  - Flujo: `POST /api/attachments/upload` → `PUT /api/shipments/{id}` con `documentUrl` → `useInvalidate`
  - Spinner durante la subida, mensajes de éxito/error

### Validación
- [x] Subir un PDF → el link aparece en la vista de edición y en el detalle
- [x] Reemplazar un documento → la URL se actualiza

---

## FASE 5 — Detalle de envío (`ShipmentShow`)

**Objetivo:** Vista de solo lectura con todos los campos y el documento.

### Tasks
- [x] Crear `src/pages/shipments/show.tsx`
  - `useShow<Shipment>`
  - `Descriptions` responsive (`column={isMobile ? 1 : 2}`)
  - Si `documentUrl`: botón "Ver documento" (`target="_blank"`)
  - `EditButton` en el header

### Validación
- [x] El detalle muestra todos los campos del envío
- [x] El link al documento abre el archivo en una nueva pestaña

---

## FASE 6 — Índice y registro en App

**Objetivo:** Exportar todos los componentes y conectar en el router.

### Tasks
- [x] Crear `src/pages/shipments/index.ts` exportando los 4 componentes
- [x] Importar y registrar rutas en `App.tsx`

### Validación
- [x] Flujo completo: crear envío → editar → subir documento → ver detalle

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6
```
