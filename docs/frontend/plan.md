# Frontend Plan — K-Maleon (Refine + Ant Design)

> Validar cada fase antes de avanzar a la siguiente.
> El frontend depende del backend — iniciar solo cuando el backend esté en Fase 6+.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Setup del Proyecto

**Objetivo:** Proyecto Refine corriendo y conectado al API de backend.

### Tasks
- [x] Inicializar proyecto con `npm create refine-app` usando Ant Design
- [x] Configurar `VITE_API_URL` apuntando al backend (local o Railway)
- [x] Implementar custom `dataProvider` REST apuntando al Spring Boot API
- [x] Configurar routing básico con las secciones principales
- [ ] Configurar CORS en el browser (asegurarse que el backend lo permite)

### Validación
- [ ] `npm run dev` levanta sin errores
- [ ] El `dataProvider` puede hacer un `GET /api/suppliers` exitosamente

### Notas
- Data provider custom en `src/providers/data.ts` — maneja rutas `/api/{resource}`, respuestas array y paginadas
- `VITE_API_URL` con fallback a `localhost:8080`, documentado en `.env.example`
- i18n en español configurado globalmente (`src/i18n/`) via `react-i18next`

---

## FASE 2 — Proveedores (Suppliers)

**Objetivo:** CRUD básico de proveedores.

### Tasks
- [x] Lista de proveedores (`GET /api/suppliers`)
  - Tabla con columnas: Nombre, Fecha de registro
- [x] Formulario de creación (`POST /api/suppliers`)
  - Campo: Nombre (requerido)

### Validación
- [ ] Se puede crear un proveedor y aparece en la lista
- [ ] Validación de campo vacío funciona

---

## FASE 3 — Operaciones (Operations)

**Objetivo:** Vista completa de operaciones con detalle y resumen financiero.

### Tasks

#### Lista de operaciones
- [x] Tabla con columnas: Contenedor, Proveedor, Monto acordado, Pagado, Pendiente, Estado, Fecha inicio
- [x] Filtros: por status (active/completed/cancelled), por proveedor, por rango de fechas
- [x] Badge de estado (color por status)

#### Detalle de operación
- [x] Header: Contenedor, Proveedor, Descripción, Origen, Fechas, Notas
- [x] Resumen financiero (consumido del API, nunca calculado en frontend):
  ```
  Monto acordado:  $10,000
  Total pagado:     $5,000   ← solo salidas
  Pendiente:        $5,000
  ```
- [x] Tabla de movimientos de la operación (ver Fase 4)

#### Formulario crear/editar operación
- [x] Campos: Proveedor (select), Contenedor, Descripción, Monto acordado, Origen, Fecha inicio, Fecha fin, Notas, Status
- [x] Monto mostrado en USD con 2 decimales (internamente en centavos)

### Validación
- [ ] Los montos se muestran correctamente formateados (dividir entre 100)
- [ ] El resumen financiero coincide con los movimientos registrados
- [ ] Crear y editar operación funciona correctamente

### Notas
- Helper `src/utils/money.ts` — `formatUSD(cents)`, `dollarsToCents()`, `centsToDollars()`
- Tipo `Operation` en `src/types/operation.ts`

---

## FASE 4 — Movimientos (Account Movements)

**Objetivo:** Registrar y visualizar entradas y salidas por operación.

### Tasks

#### Lista de movimientos (dentro del detalle de operación)
- [x] Tabla con columnas: Fecha, Tipo (Entrada/Salida), Método de pago, Monto, Descripción, Comprobante
- [x] Color diferenciado: Entrada (verde) / Salida (rojo)

#### Formulario de nuevo movimiento
- [x] Campos base: Tipo (entrada/salida), Método de pago, Monto, Fecha, Descripción
- [x] Campos dinámicos según `payment_type`:
  - `swift`: message_id, UETR, settlement_date, debtor_bank, creditor_bank, creditor_name, etc.
  - `transfer`: banco, número de cuenta, referencia, beneficiario
  - `dhl`: tracking number, origen, destino, tipo de servicio
  - `cash`: recibido por, ubicación
  - `other`: campos libres (key-value dinámico)
- [ ] Upload de comprobante (PDF/imagen) → Supabase Storage via API
- [x] Advertencia visual al registrar una salida: "Esto reducirá el saldo de la cuenta"

### Validación
- [ ] Registrar una salida actualiza `paid_amount` en el detalle de la operación
- [ ] Registrar una entrada NO cambia `paid_amount`
- [ ] El comprobante se puede visualizar (link al PDF)
- [ ] Los campos dinámicos aparecen/desaparecen según el método de pago seleccionado

### Notas
- Modal de nuevo movimiento en `src/pages/movements/MovementCreateModal.tsx`
- Campos dinámicos en `src/pages/movements/MetadataFields.tsx`
- Tabla en `src/pages/movements/MovementTable.tsx` — fetch directo a `/api/operations/:id/movements`
- `metadata` se serializa como JSON string antes de enviar al backend
- Upload de comprobante pendiente — el campo `attachmentUrl` acepta URL manual por ahora

---

## FASE 5 — Cuenta General (Account)

**Objetivo:** Vista del saldo general y su evolución.

### Tasks
- [x] Dashboard/panel de saldo actual (`GET /api/account/balance`)
  ```
  Saldo actual: $46,000
  ```
- [ ] Desglose: entradas totales, salidas totales (consumido del API)
- [x] Setup de saldo inicial (`POST /api/account/initial-balance`) — solo para configuración inicial

### Validación
- [ ] El saldo se actualiza automáticamente tras registrar un movimiento
- [ ] El saldo inicial solo se puede configurar una vez

### Notas
- El backend (`AccountBalanceResponse`) solo devuelve `balance` — sin desglose de entradas/salidas
- Para el desglose se necesita un nuevo endpoint en el backend: `GET /api/account/summary`

---

## FASE 6 — Audit Log

**Objetivo:** Vista de trazabilidad de todas las operaciones financieras.

### Tasks
- [x] Tabla con columnas: Fecha, Acción, Entidad, ID de entidad, Usuario
- [x] Ver detalle del payload (modal con JSON formateado)
- [x] Filtros: por acción, por entidad, por rango de fechas

### Validación
- [ ] Cada movimiento registrado aparece en el audit log
- [ ] El payload muestra el snapshot completo del dato

### Notas
- Fetch directo con `useQuery` a `GET /api/audit-log` con params opcionales
- Filtros de fecha enviados como ISO 8601 con hora (requerido por el backend)
- `payload` llega como string JSON — se parsea y formatea en el modal con `JSON.stringify(..., null, 2)`
- El campo del modelo es `entityName` (no `entity`) — mapeado correctamente

---

## FASE 7 — Deploy en Cloudflare Pages

**Objetivo:** Frontend funcionando en producción.

### Tasks
- [ ] Crear proyecto en Cloudflare Pages conectado al repo `k-maleon-frontend`
- [ ] Configurar build: `npm run build` / output: `dist`
- [ ] Configurar variable de entorno `VITE_API_URL` con la URL de Railway
- [ ] Verificar auto-deploy desde `main`
- [ ] Probar flujo completo en producción

### Validación
- [ ] Flujo completo: crear proveedor → crear operación → registrar salida → ver saldo actualizado
- [ ] El PDF/comprobante se puede subir y visualizar

---

## Orden de implementación sugerido

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6 → Fase 7
```

> Prerequisito: Backend debe estar en Fase 6 (controladores funcionando) antes de iniciar Fase 3+.

---

## Siguiente feature

**Autenticación y Roles** → ver `docs/frontend/plan-auth.md`
