# Backend Plan — K-Maleon (Spring Boot)

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Setup del Proyecto

**Objetivo:** Tener el proyecto compilando y conectado a la base de datos.

### Tasks
- [x] Inicializar proyecto Spring Boot con Gradle (`build.gradle.kts`)
- [x] Configurar `application.properties` con variables de entorno
- [x] Verificar conexión a Supabase PostgreSQL (puerto 5432)
- [x] Confirmar que `spring.jpa.hibernate.ddl-auto=validate` funciona contra el schema real

### Validación
- [x] `./gradlew bootRun` levanta sin errores
- [x] Endpoint de health `/actuator/health` responde `UP`

---

## FASE 2 — Modelos (Entidades JPA)

**Objetivo:** Mapear las tablas de la base de datos como entidades Java.

### Tasks
- [x] `Supplier.java` → tabla `suppliers`
- [x] `Account.java` → tabla `accounts`
- [x] `Operation.java` → tabla `operations`
- [x] `AccountMovement.java` → tabla `account_movements`
- [x] `AuditLog.java` → tabla `audit_log`

### Reglas
- Todos los montos como `Long` (centavos, nunca `double`)
- `metadata` en `AccountMovement` como `String` (serializado JSON) o con `@Column(columnDefinition = "jsonb")`
- UUIDs como `UUID` con `@GeneratedValue`

### Validación
- [x] `./gradlew bootRun` levanta sin errores de mapping JPA
- [x] No hay warnings de schema mismatch

---

## FASE 3 — Repositorios (Spring Data JPA)

**Objetivo:** Acceso a datos básico para cada entidad.

### Tasks
- [x] `SupplierRepository`
- [x] `AccountRepository`
- [x] `OperationRepository` (con query para filtros: status, supplier_id, fechas)
- [x] `MovementRepository` (con query por `operation_id`)
- [x] `AuditLogRepository` (con filtros por entity, action, fechas)

### Validación
- [ ] Tests básicos de repositorio (save/findById) pasan

---

## FASE 4 — DTOs

**Objetivo:** Definir los objetos de entrada/salida de la API.

### Tasks
- [x] `MovementRequest.java` — payload para crear un movimiento
- [x] `OperationSummaryResponse.java` — operación + `paid_amount` + `pending_amount`
- [x] `AccountBalanceResponse.java` — saldo actual de la cuenta
- [x] `OperationRequest.java` — payload para crear/actualizar operación
- [x] `SupplierRequest.java` — payload para crear proveedor

### Validación
- [x] Todos los DTOs tienen validaciones `@NotNull`, `@NotBlank` donde corresponde

---

## FASE 5 — Servicios (Lógica de Negocio)

**Objetivo:** Implementar todas las reglas financieras del sistema.

### Tasks

#### `SupplierService`
- [x] `findAll()` — lista proveedores
- [x] `create(SupplierRequest)` — crea proveedor

#### `AccountService`
- [x] `getBalance()` — retorna saldo actual
- [x] `setInitialBalance(amount)` — solo para setup inicial
- [x] `credit(amount)` — suma al saldo (usado internamente por MovementService)
- [x] `debit(amount)` — resta del saldo (usado internamente por MovementService)

#### `OperationService`
- [x] `findAll(filters)` — lista operaciones con filtros opcionales
- [x] `findById(id)` — retorna operación con summary (paid, pending)
- [x] `create(OperationRequest)` — crea operación
- [x] `update(id, OperationRequest)` — actualiza operación

#### `MovementService` ← CRÍTICO
- [x] `registerMovement(MovementRequest)` — lógica principal `@Transactional`
  - Si `type = 'salida'`:
    1. INSERT `account_movements`
    2. UPDATE `operations.paid_amount += amount`
    3. UPDATE `accounts.balance -= amount`
    4. INSERT `audit_log`
  - Si `type = 'entrada'`:
    1. INSERT `account_movements`
    2. UPDATE `accounts.balance += amount`
    3. INSERT `audit_log`
- [x] `findByOperationId(operationId)` — lista movimientos de una operación

#### `AuditService`
- [x] `log(action, entity, entityId, payload)` — registra entrada en audit_log
- [x] `findAll(filters)` — lista audit log con filtros

### Validación
- [ ] Test unitario: registrar salida → `paid_amount` y `balance` actualizados correctamente
- [ ] Test unitario: registrar entrada → solo `balance` actualizado, `paid_amount` sin cambio
- [ ] Test unitario: fallo en balance update → rollback completo (no queda el movement)

---

## FASE 6 — Controladores (REST API)

**Objetivo:** Exponer los endpoints REST definidos en el contrato.

### Tasks

#### `SupplierController`
- [x] `GET /api/suppliers`
- [x] `POST /api/suppliers`

#### `OperationController`
- [x] `GET /api/operations` (con query params para filtros)
- [x] `GET /api/operations/{id}`
- [x] `POST /api/operations`
- [x] `PUT /api/operations/{id}`

#### `MovementController`
- [x] `GET /api/operations/{id}/movements`
- [x] `POST /api/movements`

#### `AccountController`
- [x] `GET /api/account/balance`
- [x] `POST /api/account/initial-balance`

#### `AuditController`
- [x] `GET /api/audit-log` (con filtros)

### Validación
- [x] Todos los endpoints responden correctamente con Postman/curl
- [ ] Errores devuelven respuestas estructuradas (no stacktraces)
- [x] CORS configurado para el dominio del frontend

---

## FASE 7 — Error Handling & Validación Global

**Objetivo:** Respuestas de error consistentes y manejadas.

### Tasks
- [x] `GlobalExceptionHandler` con `@ControllerAdvice`
  - [x] `ResourceNotFoundException` → 404
  - [x] `MethodArgumentNotValidException` → 400 con detalle de campos
  - [x] `HttpMessageNotReadableException` → 400 body malformado
  - [x] Errores genéricos → 500 sin stacktrace expuesto
- [x] Validar que montos nunca sean negativos o cero (`@Positive` en DTOs)

### Validación
- [x] Llamar con datos inválidos devuelve 400 con mensaje claro
- [x] Operación inexistente devuelve 404

---

## FASE 8 — Deploy en Railway

**Objetivo:** Backend funcionando en producción.

### Tasks
- [ ] Crear proyecto en Railway conectado al repo `k-maleon-backend`
- [ ] Configurar variables de entorno en Railway dashboard
- [ ] Verificar auto-deploy desde `main`
- [ ] Probar todos los endpoints contra la URL de producción

### Validación
- [ ] `GET https://<railway-url>/api/account/balance` responde correctamente
- [ ] Los logs en Railway muestran las operaciones financieras

---

## Orden de implementación sugerido

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6 → Fase 7 → Fase 8
```

Cada fase debe estar funcionando antes de avanzar a la siguiente.
