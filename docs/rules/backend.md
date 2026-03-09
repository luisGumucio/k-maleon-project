# Reglas — Backend (Spring Boot)

## Dinero
- Todos los montos se almacenan como `BIGINT` en PostgreSQL y `Long` en Java (centavos, nunca float)
- Usar `BigDecimal` para cualquier aritmética intermedia, nunca `double` o `float`
- Ejemplo: $10,000.00 → se guarda como `1000000`

## Transacciones financieras
- Toda operación de escritura financiera debe estar anotada con `@Transactional` en la capa de servicio
- Si cualquier paso falla → rollback completo (nada persiste parcialmente)
- **Salida:** afecta `operations.paid_amount` + `accounts.balance` en la misma transacción
- **Entrada:** solo afecta `accounts.balance`, nunca modifica `paid_amount`

## Audit log
- Toda operación de escritura financiera debe generar una entrada en `audit_log`
- El payload debe ser un snapshot completo del dato en ese momento

## Arquitectura
- Toda la lógica de negocio vive en la capa `Service`, nunca en controllers ni repositories
- Los controllers solo reciben la request, delegan al service y devuelven la response

## Metadata de pagos
- El campo `metadata` en `account_movements` es JSONB abierto/extensible
- Nunca hardcodear validación de campos del metadata — el sistema es abierto por diseño

## Adjuntos
- Los comprobantes (PDFs, imágenes) se guardan en Supabase Storage
- En `account_movements.attachment_url` solo se guarda la URL, nunca el archivo en la DB

## Unit Tests
- Todo cambio en un servicio debe tener su unit test correspondiente
- Los tests van en `src/test/java/com/kmaleon/service/`
- Usar `@ExtendWith(MockitoExtension.class)` — sin contexto Spring (más rápido)
- Comando para correr: `./gradlew test`
- Comando para correr un test específico: `./gradlew test --tests "com.kmaleon.service.MovementServiceTest"`
- Cada test debe cubrir obligatoriamente los tres casos:
  1. **Happy path** — el flujo exitoso esperado
  2. **Error path** — excepciones, not found, datos inválidos
  3. **Edge cases** — valores límite, nulls, defaults, acumulaciones

## Principios SOLID
- Respetar SOLID en toda la codebase
- **SRP:** cada clase tiene una sola responsabilidad — los servicios no mapean DTOs, los DTOs se construyen a sí mismos
- **OCP:** abierto para extensión, cerrado para modificación — especialmente en metadata de pagos
- **DIP:** depender de abstracciones, no de implementaciones concretas
- Usar el patrón **Builder** en los DTOs de respuesta — nunca métodos privados `toResponse()` en los servicios
- Los DTOs exponen un método estático `from(Entity)` que encapsula su propia construcción

## Comentarios en código
- No agregar comentarios inline en el código Java
- El código debe ser autoexplicativo a través de nombres claros de variables y métodos
- Excepción: comentarios de bloque solo cuando la lógica sea genuinamente no obvia

## Migraciones de base de datos
- Todo cambio en la base de datos (nueva tabla, nueva columna, índice, constraint) debe documentarse
  como un archivo SQL numerado en `docs/database/migrations/`
- Formato de nombre: `NNN_descripcion_corta.sql` (ej: `002_add_index_operations_status.sql`)
- Los archivos son inmutables — nunca editar un migration ya ejecutado, crear uno nuevo
- Ejecutar siempre en Supabase SQL Editor en orden numérico
