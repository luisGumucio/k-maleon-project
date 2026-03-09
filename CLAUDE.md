# CLAUDE.md — K-Maleon Financial System (Supplier Account Manager)

## Project Overview

A financial management system for tracking supplier operations and account movements. Each operation represents an agreement with a supplier for a specific container, and contains financial movements (payments out / salidas, income in / entradas).

This is essentially a simplified accounting system with:
- **Suppliers** (proveedores with whom the client works)
- **Operations** (supplier deals tied to a specific container, with a total agreed amount)
- **Account Movements** (entradas and salidas tied to an operation)
- **Account balance** (general client balance updated by every movement)

---

## Tech Stack

### Frontend
- **Framework:** [Refine](https://refine.dev/) (React-based admin framework)
- **UI Library:** Ant Design (via `@refinedev/antd`)
- **Language:** TypeScript
- **Hosting:** Cloudflare Pages (free tier)
- **Data Provider:** Custom REST data provider pointing to the Spring Boot API

### Backend
- **Framework:** Java Spring Boot
- **Language:** Java 17+
- **Build Tool:** Gradle (`build.gradle.kts`)
- **Hosting:** Railway (~$5/month, auto-deploy from GitHub)
- **Architecture:** REST API — all business logic and financial calculations live here
- **Logging:** SLF4J + Logback (structured logs for all financial operations)

### Database
- **Provider:** Supabase (managed PostgreSQL)
- **Connection:** Spring Boot connects via JDBC (`spring.datasource`) using port 5432 (direct connection, not pgBouncer)
- **Money type:** All monetary amounts stored as `BIGINT` (cents/integers, never float)
- **File storage:** Supabase Storage for payment attachments (PDFs, receipts)

---

## Architecture

```
User → Cloudflare Pages (Refine frontend)
            ↓ REST API calls
       Railway (Spring Boot API)
            ↓ JDBC (port 5432)
       Supabase (PostgreSQL + Storage)
```

> The frontend NEVER talks directly to Supabase. All data access goes through the Spring Boot API.

---

## Core Domain Model

### `suppliers`
Proveedores registrados con los que trabaja el cliente.

```sql
CREATE TABLE suppliers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);
```

### `accounts`
Cuenta general del cliente. Hay una sola cuenta. El saldo se actualiza con cada movimiento.

```sql
CREATE TABLE accounts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    balance    BIGINT NOT NULL DEFAULT 0,  -- saldo actual en centavos
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```

### `operations`
Representa un acuerdo con un proveedor para un contenedor específico.

```sql
CREATE TABLE operations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id   UUID NOT NULL REFERENCES suppliers(id),
    container     VARCHAR(100) NOT NULL,   -- nro contenedor ej: CARU5170029
    description   TEXT,                   -- descripción de la mercadería
    total_amount  BIGINT NOT NULL,         -- monto acordado en centavos
    paid_amount   BIGINT DEFAULT 0,        -- suma de salidas, actualizado por backend
    origin        VARCHAR(100),            -- puerto de origen ej: Shanghai
    start_date    DATE NOT NULL,
    end_date      DATE,
    status        VARCHAR(50) DEFAULT 'active',  -- active | completed | cancelled
    notes         TEXT,
    created_at    TIMESTAMPTZ DEFAULT now(),
    updated_at    TIMESTAMPTZ DEFAULT now()
);
```

### `account_movements`
Movimientos financieros. Todos van ligados a una operación (contenedor).
- **Salida:** reduce `paid_amount` de la operación Y reduce `balance` de la cuenta
- **Entrada:** aumenta `balance` de la cuenta (NO afecta `paid_amount` de la operación)

```sql
CREATE TABLE account_movements (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_id   UUID NOT NULL REFERENCES operations(id),
    type           VARCHAR(10) NOT NULL CHECK (type IN ('entrada', 'salida')),
    payment_type   VARCHAR(50),            -- swift | transfer | dhl | cash | other
    amount         BIGINT NOT NULL,        -- en centavos, siempre positivo
    currency       VARCHAR(10) DEFAULT 'USD',
    date           DATE NOT NULL,
    description    TEXT,
    metadata       JSONB,                  -- datos específicos según payment_type
    attachment_url TEXT,                   -- URL del PDF/comprobante en Supabase Storage
    created_by     UUID,
    created_at     TIMESTAMPTZ DEFAULT now()
);
```

### `audit_log`
Registro inmutable de toda operación financiera.

```sql
CREATE TABLE audit_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID,
    action     VARCHAR(100) NOT NULL,  -- movement_created | operation_updated | etc.
    entity     VARCHAR(100) NOT NULL,  -- operations | account_movements | accounts
    entity_id  UUID,
    payload    JSONB,                  -- snapshot completo del dato en ese momento
    created_at TIMESTAMPTZ DEFAULT now()
);
```

---

## Payment Types & Metadata

El campo `metadata` en `account_movements` es JSONB y varía según `payment_type`. El sistema es abierto: si aparece un nuevo tipo, solo se agrega sin cambiar la estructura.

```json
// payment_type: "swift"
{
  "message_id": "22591270001",
  "uetr": "9b6df95f-ce82-4871-b067-3e4d6d1b1ff1",
  "currency": "USD",
  "settlement_date": "2026-03-02",
  "debtor_bank": "SCOTIABANK CHILE",
  "debtor_bic": "BKSACLRMXXX",
  "debtor_account": "90993132187",
  "creditor_bank": "JPMORGAN CHASE BANK HK",
  "creditor_bic": "CHASHKHHXXX",
  "creditor_name": "YIWU XUANSHI E-COMERCE FIRM",
  "creditor_account": "63003673626",
  "remittance": "PAGO DE MERCADERIA",
  "charge_bearer": "DEBT"
}

// payment_type: "transfer"
{
  "bank_name": "Banco Estado",
  "account_number": "123456789",
  "reference": "TRF-2026-001",
  "beneficiary": "China Trading Co."
}

// payment_type: "dhl"
{
  "tracking_number": "1234567890",
  "origin": "Santiago",
  "destination": "Hong Kong",
  "service_type": "EXPRESS"
}

// payment_type: "cash"
{
  "received_by": "Juan Pérez",
  "location": "Oficina Iquique"
}

// payment_type: "other" (abierto, el cliente define los campos)
{
  "custom_type": "descripción libre",
  "any_field": "valor"
}
```

---

## Financial Logic Rules

### Reglas de balance (solo en el backend, nunca en el frontend)

```
-- Por operación:
pending_amount = total_amount - paid_amount

-- Cuenta general:
account_balance = saldo_inicial + SUM(entradas) - SUM(salidas)
```

### Cuando se registra una `salida`:
Afecta DOS cosas en una sola transacción:
1. `INSERT` into `account_movements` (type = 'salida')
2. `UPDATE operations SET paid_amount = paid_amount + amount` → reduce deuda con proveedor
3. `UPDATE accounts SET balance = balance - amount` → reduce saldo del cliente
4. `INSERT` into `audit_log`
5. **Rollback completo si cualquier paso falla**

### Cuando se registra una `entrada`:
Afecta solo la cuenta general:
1. `INSERT` into `account_movements` (type = 'entrada')
2. `UPDATE accounts SET balance = balance + amount` → aumenta saldo del cliente
3. `INSERT` into `audit_log`
4. **Las entradas NO modifican `paid_amount` de la operación**

> ⚠️ Las entradas aparecen en el historial de la operación para trazabilidad, pero NO se cuentan para determinar si la deuda está cubierta.

---

## UI Views

### Vista de operación (detalle)

```
Operación CARU5170029
Proveedor: China Trading | Monto acordado: $10,000

Fecha  | Tipo    | Método    | Monto  | Comprobante
-------|---------|-----------|--------|-------------
05/03  | Salida  | SWIFT     | $2,000 | [ver PDF]
08/03  | Entrada | Transfer  | $1,000 | [ver PDF]
10/03  | Salida  | Transfer  | $3,000 | [ver PDF]

Total pagado al proveedor:  $5,000   ← solo suma salidas
Pendiente:                  $5,000
```

### Vista de cuenta general

```
Saldo inicial:   $50,000
+ Entradas:       $1,000
- Salidas:        $5,000
─────────────────────────
Saldo actual:    $46,000
```

---

## Money Handling Rules

- **Todos los montos se guardan como enteros (centavos)** — nunca usar `float` o `double`
- Ejemplo: $10,000.00 → se guarda como `1000000`
- La API recibe y devuelve montos en centavos
- El frontend solo formatea para mostrar (divide entre 100)
- Usar `BigDecimal` en Java para cualquier aritmética intermedia, nunca `double`

---

## Spring Boot Project Structure

```
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        ├── java/com/kmaleon/
        │   ├── controller/
        │   │   ├── OperationController.java
        │   │   ├── MovementController.java
        │   │   ├── SupplierController.java
        │   │   └── AccountController.java
        │   ├── service/
        │   │   ├── OperationService.java      ← business logic here
        │   │   ├── MovementService.java       ← financial rules here
        │   │   ├── AccountService.java        ← balance management here
        │   │   └── AuditService.java          ← audit logging here
        │   ├── repository/
        │   │   ├── OperationRepository.java
        │   │   ├── MovementRepository.java
        │   │   ├── AccountRepository.java
        │   │   └── AuditLogRepository.java
        │   ├── model/
        │   │   ├── Operation.java
        │   │   ├── AccountMovement.java
        │   │   ├── Account.java
        │   │   ├── Supplier.java
        │   │   └── AuditLog.java
        │   └── dto/
        │       ├── MovementRequest.java
        │       ├── OperationSummaryResponse.java
        │       └── AccountBalanceResponse.java
        └── resources/
            └── application.properties
```

### `build.gradle.kts` base

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    java
}

group = "com.kmaleon"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

## API Endpoints

```
# Suppliers
GET    /api/suppliers                         → list suppliers
POST   /api/suppliers                         → create supplier

# Operations
GET    /api/operations                        → list all operations (with filters)
GET    /api/operations/:id                    → get operation + summary (paid, pending)
POST   /api/operations                        → create operation
PUT    /api/operations/:id                    → update operation

# Movements
GET    /api/operations/:id/movements          → list all movements for an operation
POST   /api/movements                         → register movement (salida or entrada)

# Account
GET    /api/account/balance                   → general account balance
POST   /api/account/initial-balance           → set initial balance (setup only)

# Audit
GET    /api/audit-log                         → full audit trail (with filters)
```

---

## Environment Variables

### Spring Boot (Railway)
```properties
# application.properties
server.port=${PORT:8080}
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

### Refine Frontend (Cloudflare Pages)
```
VITE_API_URL=https://your-api.up.railway.app
```

---

## Repository Structure

Two separate GitHub repos:

```
github.com/youruser/k-maleon-backend    ← Spring Boot
github.com/youruser/k-maleon-frontend   ← Refine
```

Cada repo tiene su propio CLAUDE.md si se trabaja por separado, pero este archivo aplica a ambos como referencia del sistema completo.

---

## Deployment

### Backend (Railway) ← apunta a `k-maleon-backend`
1. Crear nuevo proyecto en Railway
2. Conectar con `k-maleon-backend` en GitHub
3. Railway auto-detecta `build.gradle.kts` y compila con Gradle
4. Agregar variables de entorno en Railway dashboard:
   ```
   DATABASE_URL=postgresql://postgres:[pass]@db.xxx.supabase.co:5432/postgres
   DATABASE_USER=postgres
   DATABASE_PASSWORD=tu_password
   ```
5. Cada push a `main` dispara auto-deploy
6. Railway asigna dominio automático con HTTPS: `https://k-maleon-backend-production-xxxx.up.railway.app`

### Frontend (Cloudflare Pages) ← apunta a `k-maleon-frontend`
1. Crear nuevo proyecto en Cloudflare Pages
2. Conectar con `k-maleon-frontend` en GitHub
3. Configurar build:
   - Build command: `npm run build`
   - Output directory: `dist`
4. Agregar variable de entorno:
   ```
   VITE_API_URL=https://k-maleon-backend-production-xxxx.up.railway.app
   ```
5. Cada push a `main` dispara auto-deploy
6. Cloudflare asigna dominio automático con HTTPS: `https://k-maleon-frontend.pages.dev`

---

## Key Constraints & Rules for Claude

> Las reglas detalladas están separadas por capa. **Leer siempre antes de escribir código:**
> - Backend: `docs/rules/backend.md`
> - Frontend: `docs/rules/frontend.md`

### Resumen de reglas críticas

1. **Never calculate money on the frontend** — always consume pre-calculated values from the API
2. **Never use float/double for monetary amounts** — use `BigDecimal` in Java, `BIGINT` in PostgreSQL
3. **Every financial write operation must be wrapped in `@Transactional`** in the service layer
4. **Every financial write operation must produce an audit log entry**
5. **All business logic lives in the Service layer**, not in controllers or repositories
6. **The frontend (Refine) only calls the Spring Boot API**, never Supabase directly
7. **Rollback on any failure** — if the movement insert succeeds but the balance update fails, nothing persists
8. **Salidas affect both `operations.paid_amount` AND `accounts.balance`** in the same transaction
9. **Entradas only affect `accounts.balance`** — they never modify `paid_amount` of an operation
10. **All movements are tied to an operation** — there are no free-floating movements without a container
11. **payment_type metadata is open/extensible** — never hardcode field validation for metadata JSONB
12. **Use Supabase Storage for attachments** — store only the URL in `account_movements.attachment_url`
13. **Every database change (new table, column, index, constraint) must be documented** as a numbered SQL file in `docs/database/migrations/` — format: `NNN_short_description.sql`
