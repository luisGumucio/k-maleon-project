# Backend Plan — Feature: Shipments

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## FASE 1 — Base de Datos

**Objetivo:** Crear la tabla `shipments` en Supabase.

### Tasks
- [x] Crear migration `docs/database/migrations/002_create_shipments.sql`
- [ ] Ejecutar el SQL en Supabase (SQL Editor)

### Validación
- [ ] La tabla `shipments` existe en la base de datos
- [ ] `./gradlew bootRun` levanta sin errores de schema (`ddl-auto=validate`)

---

## FASE 2 — Modelo JPA

**Objetivo:** Mapear la tabla `shipments` como entidad Java.

### Tasks
- [x] Crear `Shipment.java` en `com.kmaleon.model`
  - `number` anotado con `@Column(insertable=false, updatable=false)` — lo genera la DB

### Validación
- [ ] `./gradlew bootRun` sin errores de mapping JPA

---

## FASE 3 — Repositorio

**Objetivo:** Acceso a datos con filtro por proveedor.

### Tasks
- [x] Crear `ShipmentRepository.java` extendiendo `JpaRepository<Shipment, UUID>` y `JpaSpecificationExecutor<Shipment>`

### Validación
- [ ] Compila sin errores

---

## FASE 4 — DTOs

**Objetivo:** Definir los objetos de entrada y salida de la API.

### Tasks
- [x] `ShipmentRequest.java` — campos editables, `supplierId` con `@NotNull`
- [x] `ShipmentResponse.java` — respuesta con `supplierName` (join), todos los campos, `number`

### Validación
- [ ] Compilan sin errores

---

## FASE 5 — Servicio

**Objetivo:** Lógica de negocio para shipments.

### Tasks
- [x] Crear `ShipmentService.java`
  - `findAll(UUID supplierId, String containerNumber, LocalDate from, LocalDate to)` — filtros opcionales
    - `containerNumber`: LIKE case-insensitive sobre `container_number`
    - `from` / `to`: rango sobre `departure_date`
  - `findById(UUID id)` — detalle o `ResourceNotFoundException`
  - `create(ShipmentRequest)` — valida supplier
  - `update(UUID id, ShipmentRequest)` — actualiza campos, incluido `documentUrl` (solo si no es null)
  - `delete(UUID id)` — elimina o `ResourceNotFoundException`

### Validación
- [ ] Compila sin errores

---

## FASE 6 — StorageService: soporte multi-bucket

**Objetivo:** `upload` y `delete` aceptan bucket como parámetro.

### Tasks
- [x] Modificar `StorageService.upload(file, bucket)` — firma actualizada
- [x] Modificar `StorageService.delete(url, bucket)` — firma actualizada
- [x] Exponer constantes `BUCKET_FINANCIAL` y `BUCKET_CONTAINER` en `StorageService`
- [x] Actualizar `AttachmentController` para pasar `StorageService.BUCKET_FINANCIAL`
- [x] Actualizar `MovementService` para pasar `StorageService.BUCKET_FINANCIAL` en compensación

### Validación
- [ ] Tests existentes de `StorageService` pasan con la nueva firma

---

## FASE 7 — Controlador

**Objetivo:** Exponer los 5 endpoints REST.

### Tasks
- [x] Crear `ShipmentController.java`
  - `GET /api/shipments` — lista con query params opcionales: `supplierId`, `containerNumber`, `from`, `to`
  - `GET /api/shipments/{id}` — detalle
  - `POST /api/shipments` — crear (`201 Created`)
  - `PUT /api/shipments/{id}` — actualizar
  - `DELETE /api/shipments/{id}` — eliminar (`204 No Content`)

### Validación
- [ ] Todos los endpoints responden correctamente con Postman
- [ ] Crear shipment → `number` autoincremental aparece en la respuesta

---

## FASE 8 — Tests Unitarios

**Objetivo:** Cobertura de la lógica del servicio.

### Tasks
- [x] Actualizar `StorageServiceTest` — firma `upload(file, bucket)` y `delete(url, bucket)`
  - Nuevo test: `upload_withContainerBucket_returnsUrlWithContainerBucket`
- [x] Actualizar `MovementServiceTest` — `verify(storageService).delete(url, BUCKET_FINANCIAL)`
- [x] Crear `ShipmentServiceTest.java` con `@ExtendWith(MockitoExtension.class)`
  - Happy path: create → response con supplierName
  - Happy path: update con documentUrl → persiste
  - Happy path: findAll, delete
  - Error path: supplier no existe → `ResourceNotFoundException`
  - Error path: shipment no existe en update/delete → `ResourceNotFoundException`
  - Edge case: update sin documentUrl → no sobreescribe el existente

### Validación
- [ ] Todos los tests pasan con `./gradlew test` sin levantar la app

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6 → Fase 7 → Fase 8
```
