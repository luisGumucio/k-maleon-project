# CLAUDE.md — K-Maleon Financial System

## Project Overview

Sistema de gestión financiera para rastrear operaciones con proveedores y movimientos de cuenta. Cada operación representa un acuerdo con un proveedor para un contenedor específico, con movimientos financieros (entradas / salidas) y seguimiento de embarques con sus productos.

---

## Tech Stack

### Frontend
- **Framework:** Refine (React-based admin framework)
- **UI Library:** Ant Design (`@refinedev/antd`)
- **Language:** TypeScript
- **Hosting:** Cloudflare Pages
- **Data Provider:** Custom REST provider → `frontend/src/providers/data.ts`

### Backend
- **Framework:** Java Spring Boot 3.2
- **Language:** Java 17+
- **Build Tool:** Gradle (`build.gradle.kts`)
- **Hosting:** Railway (auto-deploy desde GitHub)
- **Architecture:** REST API — toda la lógica de negocio y cálculos financieros viven aquí

### Database
- **Provider:** Supabase (PostgreSQL)
- **Connection:** JDBC directo, puerto 5432 (no pgBouncer)
- **File storage:** Supabase Storage (PDFs, comprobantes, documentos de contenedor)

---

## Architecture

```
User → Cloudflare Pages (Refine frontend)
            ↓ REST API calls
       Railway (Spring Boot API)
            ↓ JDBC (port 5432)
       Supabase (PostgreSQL + Storage)
```

> El frontend NUNCA habla directamente con Supabase. Todo pasa por el API de Spring Boot.

---

## Referencias de documentación

### Base de datos
- **Schema completo (tablas base):** `docs/database/migrations/001_init_schema.sql`
- **Tabla `shipments`:** `docs/database/migrations/002_create_shipments.sql`
- **Tabla `shipment_items`:** `docs/database/migrations/003_create_shipment_items.sql`
- > Toda nueva tabla/columna/índice debe agregarse como `NNN_short_description.sql` en `docs/database/migrations/`

### API Endpoints
- **Colección Postman completa:** `docs/postman/k-maleon-api.postman_collection.json`
  - Incluye: Account, Suppliers, Operations, Movements, Attachments, Shipments, Shipment Items, Audit Log

### Reglas por capa
- **Backend:** `docs/rules/backend.md`
- **Frontend:** `docs/rules/frontend.md`

### Features implementados
- **Shipments (Rastreo):** `docs/features/01-shipment.md`
- **Shipment Items (Contenidos):** `docs/features/02-shipment-items.md`

### Planes de trabajo
- **Backend — Shipments:** `docs/backend/plan-shipments.md`
- **Backend — Shipment Items:** `docs/backend/plan-shipment-items.md`
- **Frontend — Shipments:** `docs/frontend/plan-shipments.md`
- **Frontend — Shipment Items:** `docs/frontend/plan-shipment-items.md`
- **Frontend — General:** `docs/frontend/plan.md`
- **Backend — General:** `docs/backend/plan.md`

### Bugs documentados
- `docs/bugs/` — registro de bugs conocidos con contexto y solución

---

## Reglas críticas (resumen)

1. **Nunca calcular dinero en el frontend** — consumir valores pre-calculados del API
2. **Nunca usar float/double para montos** — `BigDecimal` en Java, `BIGINT` en PostgreSQL (centavos)
   - Excepción: `shipment_items.amount` y `unit_price` son `NUMERIC` (precios de producto, no centavos)
3. **Toda escritura financiera va en `@Transactional`** en la capa de servicio
4. **Toda escritura financiera produce un audit log**
5. **Toda la lógica de negocio vive en el Service layer**, no en controllers ni repositories
6. **El frontend solo llama al Spring Boot API**, nunca a Supabase directamente
7. **Rollback completo ante cualquier falla** en transacciones financieras
8. **Salidas afectan `operations.paid_amount` Y `accounts.balance`** en la misma transacción
9. **Entradas solo afectan `accounts.balance`** — nunca `paid_amount` de la operación
10. **Los attachments van a Supabase Storage** — solo se guarda la URL en la base de datos
    - Bucket financiero: `financial-docs` (comprobantes de movimientos)
    - Bucket contenedor: `container-docs` (documentos de shipments)
11. **StorageService** es el único punto de acceso a Supabase Storage — usar `upload(file, bucket)` y `delete(url, bucket)`

---

## Environment Variables

### Backend (Railway)
```properties
server.port=${PORT:8080}
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

### Frontend (Cloudflare Pages)
```
VITE_API_URL=https://your-api.up.railway.app
```
> Ver `frontend/.env.example` para referencia local.
