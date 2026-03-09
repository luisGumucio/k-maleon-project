# Backend Plan — Feature: Shipment Items

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Base de Datos

**Objetivo:** Crear la tabla `shipment_items` en Supabase.

### Tasks
- [x] Crear `docs/database/migrations/003_create_shipment_items.sql`
- [ ] Ejecutar el SQL en Supabase (SQL Editor)

### Validación
- [ ] La tabla `shipment_items` existe en la base de datos
- [ ] `./gradlew bootRun` levanta sin errores de schema (`ddl-auto=validate`)

---

## FASE 2 — Modelo JPA

**Objetivo:** Mapear la tabla `shipment_items` como entidad Java.

### Tasks
- [x] Crear `ShipmentItem.java` en `com.kmaleon.model`
  - `shipment` ManyToOne lazy, `amount`/`unitPrice` como `BigDecimal`

### Validación
- [ ] `./gradlew bootRun` sin errores de mapping JPA

---

## FASE 3 — Repositorio

**Objetivo:** Acceso a datos filtrado por `shipment_id`.

### Tasks
- [x] Crear `ShipmentItemRepository.java`
  - `findByShipmentIdOrderByCreatedAtAsc(UUID shipmentId)`

### Validación
- [ ] Compila sin errores

---

## FASE 4 — DTOs

**Objetivo:** Definir los objetos de entrada y salida.

### Tasks
- [x] `ShipmentItemRequest.java` — `shipmentId` (`@NotNull`), `description` (`@NotBlank`), `amount` (`@NotNull`, `@Positive`), `quantity` y `unitPrice` opcionales
- [x] `ShipmentItemResponse.java` — incluye `shipmentId` y `containerNumber`
- [x] `ShipmentDetailResponse.java` — datos del shipment + `items` + `totalAmount` (`SUM(amount)` calculado en el factory method)

### Validación
- [ ] Compilan sin errores

---

## FASE 5 — Servicio

**Objetivo:** Lógica de negocio para ítems.

### Tasks
- [x] Crear `ShipmentItemService.java`
  - `findByShipmentId` → valida shipment, retorna `ShipmentDetailResponse` con total
  - `create` → valida shipment, inserta ítem
  - `update` → actualiza campos o `ResourceNotFoundException`
  - `delete` → elimina o `ResourceNotFoundException`
- `totalAmount` nunca se persiste — se calcula en `ShipmentDetailResponse.from()`

### Validación
- [ ] Compila sin errores

---

## FASE 6 — Controlador

**Objetivo:** Exponer los 4 endpoints REST.

### Tasks
- [x] Crear `ShipmentItemController.java`
  - `GET /api/shipment-items?shipmentId={id}` → `ShipmentDetailResponse`
  - `POST /api/shipment-items` → `201 Created`, `ShipmentItemResponse`
  - `PUT /api/shipment-items/{id}` → `ShipmentItemResponse`
  - `DELETE /api/shipment-items/{id}` → `204 No Content`

### Validación
- [ ] Todos los endpoints responden correctamente con Postman
- [ ] `GET` con `shipmentId` inexistente → `404 Not Found`

---

## FASE 7 — Tests Unitarios

**Objetivo:** Cobertura de la lógica del servicio con Mockito, sin Spring context.

### Tasks
- [x] Crear `ShipmentItemServiceTest.java` con `@ExtendWith(MockitoExtension.class)`
  - Happy path: `findByShipmentId` con ítems y total correcto
  - Happy path: `create`, `update`, `delete`
  - Error path: shipment/item inexistente → `ResourceNotFoundException`
  - Edge case: shipment sin ítems → lista vacía y `totalAmount = 0`
  - Edge case: total de 3 ítems (`500 + 200 + 480 = 1180`)

### Validación
- [ ] Todos los tests pasan con `./gradlew test` sin levantar la app

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6 → Fase 7
```
