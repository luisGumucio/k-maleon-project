# Frontend Plan — K-Maleon (Refine + Ant Design)

> Validar cada fase antes de avanzar a la siguiente.
> El frontend depende del backend — iniciar solo cuando el backend esté en Fase 6+.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Setup del Proyecto

**Objetivo:** Proyecto Refine corriendo y conectado al API de backend.

### Tasks
- [ ] Inicializar proyecto con `npm create refine-app` usando Ant Design
- [ ] Configurar `VITE_API_URL` apuntando al backend (local o Railway)
- [ ] Implementar custom `dataProvider` REST apuntando al Spring Boot API
- [ ] Configurar routing básico con las secciones principales
- [ ] Configurar CORS en el browser (asegurarse que el backend lo permite)

### Validación
- [ ] `npm run dev` levanta sin errores
- [ ] El `dataProvider` puede hacer un `GET /api/suppliers` exitosamente

---

## FASE 2 — Proveedores (Suppliers)

**Objetivo:** CRUD básico de proveedores.

### Tasks
- [ ] Lista de proveedores (`GET /api/suppliers`)
  - Tabla con columnas: Nombre, Fecha de registro
- [ ] Formulario de creación (`POST /api/suppliers`)
  - Campo: Nombre (requerido)

### Validación
- [ ] Se puede crear un proveedor y aparece en la lista
- [ ] Validación de campo vacío funciona

---

## FASE 3 — Operaciones (Operations)

**Objetivo:** Vista completa de operaciones con detalle y resumen financiero.

### Tasks

#### Lista de operaciones
- [ ] Tabla con columnas: Contenedor, Proveedor, Monto acordado, Pagado, Pendiente, Estado, Fecha inicio
- [ ] Filtros: por status (active/completed/cancelled), por proveedor, por rango de fechas
- [ ] Badge de estado (color por status)

#### Detalle de operación
- [ ] Header: Contenedor, Proveedor, Descripción, Origen, Fechas, Notas
- [ ] Resumen financiero (consumido del API, nunca calculado en frontend):
  ```
  Monto acordado:  $10,000
  Total pagado:     $5,000   ← solo salidas
  Pendiente:        $5,000
  ```
- [ ] Tabla de movimientos de la operación (ver Fase 4)

#### Formulario crear/editar operación
- [ ] Campos: Proveedor (select), Contenedor, Descripción, Monto acordado, Origen, Fecha inicio, Fecha fin, Notas, Status
- [ ] Monto mostrado en USD con 2 decimales (internamente en centavos)

### Validación
- [ ] Los montos se muestran correctamente formateados (dividir entre 100)
- [ ] El resumen financiero coincide con los movimientos registrados
- [ ] Crear y editar operación funciona correctamente

---

## FASE 4 — Movimientos (Account Movements)

**Objetivo:** Registrar y visualizar entradas y salidas por operación.

### Tasks

#### Lista de movimientos (dentro del detalle de operación)
- [ ] Tabla con columnas: Fecha, Tipo (Entrada/Salida), Método de pago, Monto, Descripción, Comprobante
- [ ] Color diferenciado: Entrada (verde) / Salida (rojo)

#### Formulario de nuevo movimiento
- [ ] Campos base: Tipo (entrada/salida), Método de pago, Monto, Fecha, Descripción
- [ ] Campos dinámicos según `payment_type`:
  - `swift`: message_id, UETR, settlement_date, debtor_bank, creditor_bank, creditor_name, etc.
  - `transfer`: banco, número de cuenta, referencia, beneficiario
  - `dhl`: tracking number, origen, destino, tipo de servicio
  - `cash`: recibido por, ubicación
  - `other`: campos libres (key-value dinámico)
- [ ] Upload de comprobante (PDF/imagen) → Supabase Storage via API
- [ ] Advertencia visual al registrar una salida: "Esto reducirá el saldo de la cuenta"

### Validación
- [ ] Registrar una salida actualiza `paid_amount` en el detalle de la operación
- [ ] Registrar una entrada NO cambia `paid_amount`
- [ ] El comprobante se puede visualizar (link al PDF)
- [ ] Los campos dinámicos aparecen/desaparecen según el método de pago seleccionado

---

## FASE 5 — Cuenta General (Account)

**Objetivo:** Vista del saldo general y su evolución.

### Tasks
- [ ] Dashboard/panel de saldo actual (`GET /api/account/balance`)
  ```
  Saldo actual: $46,000
  ```
- [ ] Desglose: entradas totales, salidas totales (consumido del API)
- [ ] Setup de saldo inicial (`POST /api/account/initial-balance`) — solo para configuración inicial

### Validación
- [ ] El saldo se actualiza automáticamente tras registrar un movimiento
- [ ] El saldo inicial solo se puede configurar una vez

---

## FASE 6 — Audit Log

**Objetivo:** Vista de trazabilidad de todas las operaciones financieras.

### Tasks
- [ ] Tabla con columnas: Fecha, Acción, Entidad, ID de entidad, Usuario
- [ ] Ver detalle del payload (modal con JSON formateado)
- [ ] Filtros: por acción, por entidad, por rango de fechas

### Validación
- [ ] Cada movimiento registrado aparece en el audit log
- [ ] El payload muestra el snapshot completo del dato

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
