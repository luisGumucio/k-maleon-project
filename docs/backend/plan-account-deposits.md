# Backend Plan — Depósitos de Saldo (Account Deposits)

> Feature: poder agregar saldo manualmente a la cuenta con historial de esos depósitos.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## Contexto

Actualmente `AccountService` solo tiene `setInitialBalance` (configuración única) y los métodos internos `credit/debit` que usa `MovementService`. No existe un flujo para que el usuario agregue fondos a la cuenta de forma explícita con trazabilidad propia.

La tabla `account_movements` ya existe pero está ligada a `operation_id NOT NULL`, por lo que no puede registrar depósitos directos sin una operación asociada.

---

## FASE 1 — Migración de base de datos

**Objetivo:** Hacer `operation_id` nullable en `account_movements` para soportar depósitos sin operación.

### Tasks
- [x] Crear `016_account_deposits.sql` en `docs/database/migrations/`
  ```sql
  -- Hacer operation_id nullable para depósitos directos
  ALTER TABLE account_movements
    ALTER COLUMN operation_id DROP NOT NULL;

  -- Tabla de depósitos de saldo para historial limpio
  CREATE TABLE account_deposits (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID NOT NULL REFERENCES accounts(id),
    amount      BIGINT NOT NULL CHECK (amount > 0),
    description TEXT,
    date        DATE NOT NULL,
    created_by  UUID REFERENCES auth.users(id),
    created_at  TIMESTAMPTZ DEFAULT now()
  );

  CREATE INDEX idx_account_deposits_account_id ON account_deposits(account_id);
  CREATE INDEX idx_account_deposits_created_at ON account_deposits(created_at DESC);
  ```

> **Decisión de diseño:** Se usa tabla `account_deposits` separada (en lugar de reutilizar `account_movements`) porque:
> - Los depósitos no tienen operación, método de pago, comprobante, ni metadata SWIFT
> - Mantiene `account_movements` limpia (solo movimientos de operaciones)
> - Historial propio y consulta simple

### Validación
- [ ] Ejecutar migration en Supabase sin errores ← **pendiente (ejecutar manualmente)**
- [ ] `bootRun` con `ddl-auto=validate` levanta sin schema mismatch ← **pendiente (ejecutar manualmente)**

---

## FASE 2 — Entidad y Repositorio

### Tasks

#### `AccountDeposit.java`
- [x] Crear entidad JPA mapeada a `account_deposits`
  ```java
  @Entity
  @Table(name = "account_deposits")
  public class AccountDeposit {
      @Id @GeneratedValue UUID id;
      @Column(name = "account_id") UUID accountId;
      Long amount;        // centavos
      String description;
      LocalDate date;
      @Column(name = "created_by") UUID createdBy;
      @Column(name = "created_at") OffsetDateTime createdAt;
  }
  ```

#### `AccountDepositRepository.java`
- [x] `findByAccountIdOrderByCreatedAtDesc(UUID accountId)` → `List<AccountDeposit>`

### Validación
- [ ] `bootRun` levanta sin errores de mapping JPA

---

## FASE 3 — DTOs

### Tasks

#### `AccountDepositRequest.java`
- [ ] Campos:
  - `amount` (Long, `@Positive`, requerido) — en centavos
  - `description` (String, opcional)
  - `date` (LocalDate, requerido)

#### `AccountDepositResponse.java`
- [ ] Campos: `id`, `accountId`, `amount`, `description`, `date`, `createdAt`
- [ ] Método estático `from(AccountDeposit)`

#### `AccountSummaryResponse.java`
- [ ] Para el endpoint `GET /api/account/summary`
- [ ] Campos: `balance`, `totalDeposits`, `totalEntradas`, `totalSalidas`, `updatedAt`
  - `totalDeposits` = suma histórica de `account_deposits`
  - `totalEntradas` = suma de `account_movements` donde `type = 'entrada'`
  - `totalSalidas` = suma de `account_movements` donde `type = 'salida'`

### Validación
- [ ] DTOs compilan sin errores

---

## FASE 4 — Servicio

**Objetivo:** Lógica de negocio para depósitos, `@Transactional`.

### Tasks

#### `AccountService` — nuevos métodos
- [ ] `deposit(UUID callerId, AccountDepositRequest request)` → `AccountDepositResponse`
  1. Obtener/crear cuenta del caller (`getOrCreateAccount`)
  2. Validar `request.amount > 0` (ya garantizado por `@Positive`)
  3. INSERT en `account_deposits`
  4. UPDATE `accounts.balance += amount` (usar `credit()` existente)
  5. INSERT en `audit_log` (acción: `ACCOUNT_DEPOSIT`)
  6. Retornar `AccountDepositResponse`

- [ ] `getDeposits(UUID callerId)` → `List<AccountDepositResponse>`
  1. Obtener cuenta del caller
  2. `depositRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())`
  3. Mapear a response

- [ ] `getSummary(UUID callerId)` → `AccountSummaryResponse`
  1. Obtener cuenta del caller
  2. Calcular `totalDeposits` con query a `account_deposits`
  3. Calcular `totalEntradas` y `totalSalidas` con queries a `account_movements`
  4. Retornar summary

> Queries de suma en repositorios usando `@Query` con `SUM(a.amount)` y `Optional<Long>` para manejar null cuando no hay registros.

### Reglas críticas
- Todo en `@Transactional`
- Rollback ante cualquier falla (si credit falla, el deposit no persiste)
- Auditar con snapshot del depósito

### Validación
- [ ] Test unitario: deposit → balance incrementa correctamente
- [ ] Test unitario: fallo en credit → rollback (deposit no persiste)

---

## FASE 5 — Controlador

### Tasks

#### `AccountController` — nuevos endpoints
- [ ] `POST /api/account/deposit`
  - Body: `AccountDepositRequest`
  - Response: `AccountDepositResponse` (201 Created)
  - `@PreAuthorize(Roles.ADMIN_OR_SUPER)`

- [ ] `GET /api/account/deposits`
  - Response: `List<AccountDepositResponse>`
  - `@PreAuthorize(Roles.ADMIN_OR_SUPER)`

- [ ] `GET /api/account/summary`
  - Response: `AccountSummaryResponse`
  - `@PreAuthorize(Roles.ADMIN_OR_SUPER)`

### Validación
- [ ] `POST /api/account/deposit` con amount válido → 201, balance actualizado
- [ ] `GET /api/account/deposits` → lista ordenada por fecha desc
- [ ] `GET /api/account/summary` → totales correctos
- [ ] `POST` con amount negativo o 0 → 400

---

## FASE 6 — Postman Collection

- [ ] Agregar a `docs/postman/k-maleon-api.postman_collection.json`:
  - `POST {{baseUrl}}/api/account/deposit`
  - `GET {{baseUrl}}/api/account/deposits`
  - `GET {{baseUrl}}/api/account/summary`

---

## Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `docs/database/migrations/016_account_deposits.sql` | Crear |
| `model/AccountDeposit.java` | Crear |
| `repository/AccountDepositRepository.java` | Crear |
| `dto/AccountDepositRequest.java` | Crear |
| `dto/AccountDepositResponse.java` | Crear |
| `dto/AccountSummaryResponse.java` | Crear |
| `service/AccountService.java` | Modificar (agregar 3 métodos) |
| `controller/AccountController.java` | Modificar (agregar 3 endpoints) |
| `docs/postman/k-maleon-api.postman_collection.json` | Modificar |

---

## Orden de implementación

```
Fase 1 (DB) → Fase 2 (Entidad + Repo) → Fase 3 (DTOs) → Fase 4 (Service) → Fase 5 (Controller) → Fase 6 (Postman)
```
