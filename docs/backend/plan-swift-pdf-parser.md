# Backend Plan — Feature: SWIFT PDF Parser

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## Contexto

Al crear un movimiento de tipo SWIFT, el usuario puede subir el comprobante PDF (ya existe en `POST /api/attachments/upload`). Una vez subido y obtenida la `url`, el frontend puede llamar a `POST /api/attachments/parse-swift` enviando esa URL. El backend descarga el PDF desde Supabase Storage, extrae el texto con PDFBox y retorna los campos del metadata SWIFT pre-parseados para que el frontend los rellene en el formulario.

---

## Regla adicional

> **Siempre que se agregue o modifique un endpoint, actualizar `docs/postman/k-maleon-api.postman_collection.json`.**

---

## FASE 1 — Dependencia PDFBox

**Objetivo:** Agregar Apache PDFBox al proyecto.

### Tasks
- [x] Agregar en `backend/build.gradle.kts`:
  ```kotlin
  implementation("org.apache.pdfbox:pdfbox:3.0.3")
  ```

### Validación
- [ ] `./gradlew dependencies` resuelve `pdfbox` sin conflictos
- [ ] `./gradlew compileJava` pasa sin errores

---

## FASE 2 — DTO de respuesta

**Objetivo:** Definir el contrato de salida del endpoint.

### Tasks
- [x] Crear `SwiftMetadataResponse.java` en `com.kmaleon.dto`
  - Campos (todos `String`, todos opcionales — pueden ser `null` si no se encontró):
    - `messageId`
    - `uetr`
    - `settlementDate` — formato `YYYY-MM-DD`
    - `debtorBank`
    - `debtorBic`
    - `debtorAccount`
    - `creditorBank`
    - `creditorBic`
    - `creditorName`
    - `creditorAccount`
    - `remittance`
    - `chargeBearer`

### Validación
- [ ] Compila sin errores

---

## FASE 3 — SwiftPdfParser (lógica de extracción)

**Objetivo:** Clase de servicio puro que extrae texto del PDF y parsea cada campo con regex.

### Tasks
- [x] Crear `SwiftPdfParser.java` en `com.kmaleon.service`
  - Método principal: `SwiftMetadataResponse parse(InputStream pdfStream)`
  - Extrae todo el texto con `PDDocument.load(pdfStream)` + `PDFTextStripper`
  - Parsea cada campo con regex sobre el texto completo:

| Campo | Regex |
|---|---|
| `messageId` | `Message Identification\s+(\S+)` |
| `uetr` | `UETR\s+([a-f0-9\-]{36})` |
| `settlementDate` | `Interbank Settlement Date\s+(\d{2}\.[\w]+,\d{2})` → normalizar a `YYYY-MM-DD` |
| `debtorBic` | En sección `Debtor Agent` → `BICFI\s+(\S+)` |
| `debtorBank` | Línea siguiente al BIC del deudor |
| `debtorAccount` | `Debtor Account[\s\S]*?Identification\s+(\S+)` |
| `creditorBic` | En sección `Creditor Agent` → `BICFI\s+(\S+)` |
| `creditorBank` | Línea siguiente al BIC del acreedor |
| `creditorName` | `Creditor\s+Name\s+(.+)` |
| `creditorAccount` | `Creditor Account[\s\S]*?Identification\s+(\S+)` |
| `remittance` | `Unstructured\s+(.+)` |
| `chargeBearer` | `Charge Bearer\s+(\S+)` → extraer solo el código antes de ` -` |

  - Si un campo no se encuentra → retornar `null` para ese campo (no lanzar excepción)
  - El parser es stateless y no tiene dependencias de Spring — facilita tests unitarios

### Validación
- [ ] Compila sin errores
- [ ] Parseando el PDF de muestra (`MTSWIFT_22591270001...`) retorna los 12 campos correctamente

---

## FASE 4 — Endpoint parse-swift

**Objetivo:** Exponer `POST /api/attachments/parse-swift`.

### Tasks
- [x] Crear `ParseSwiftRequest.java` en `com.kmaleon.dto`
  - Campo: `String url` (requerido, no blank)

- [x] Agregar método en `AttachmentController.java`:
  ```
  POST /api/attachments/parse-swift
  Body: { "url": "https://..." }
  Response: SwiftMetadataResponse (200 OK)
  ```
  - Inyectar `SwiftPdfParser` en el controller
  - Descargar el PDF desde la URL usando `URL.openStream()` (HTTP GET simple)
  - Pasar el stream a `SwiftPdfParser.parse(stream)`
  - Retornar el resultado directamente
  - Si la URL no es accesible o el PDF falla → `400 Bad Request` con mensaje claro

- [x] Actualizar `docs/postman/k-maleon-api.postman_collection.json`
  - Agregar request `Parse SWIFT PDF` en la carpeta `Attachments`
  - Método: `POST`
  - URL: `{{baseUrl}}/api/attachments/parse-swift`
  - Body JSON: `{ "url": "{{pdfUrl}}" }`
  - Ejemplo de response exitoso con los 12 campos

### Validación
- [ ] `POST /api/attachments/parse-swift` con URL del PDF de prueba retorna los 12 campos correctos
- [ ] `POST /api/attachments/parse-swift` con URL inválida retorna `400`
- [ ] El endpoint requiere `Authorization: Bearer <token>`

---

## FASE 5 — Tests Unitarios

**Objetivo:** Cobertura del parser.

### Tasks
- [x] Crear `SwiftPdfParserTest.java` en `src/test`
  - Usar el PDF de muestra como recurso de test (`src/test/resources/swift-sample.pdf`)
  - Test: `parse_withValidSwiftPdf_returnsAllFields` — verifica los 12 campos del PDF de ejemplo
  - Test: `parse_withEmptyPdf_returnsAllNulls` — PDF vacío no lanza excepción
  - Test: `parse_withMissingUetr_returnsNullUetr` — campo faltante retorna null

### Validación
- [ ] `./gradlew test` pasa sin errores

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5
```
