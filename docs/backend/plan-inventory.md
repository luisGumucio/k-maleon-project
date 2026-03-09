# Plan Backend: Feature 05 — Bodega Central e Inventario

## Estado: IMPLEMENTADO

---

## Fases completadas

### B1 — Migraciones SQL ✅
Archivos en `docs/database/migrations/`:
- `004_create_units.sql`
- `005_create_items.sql`
- `006_create_unit_conversions.sql`
- `007_create_locations.sql`
- `008_create_inventory_stock.sql`
- `009_create_inventory_movements.sql`
- `010_create_transfer_requests.sql`

### B2 — Units y Locations ✅
**Units** — `GET /api/units`, `POST /api/units`, `DELETE /api/units/{id}`
- Delete valida que no tenga conversiones asociadas antes de eliminar

**Locations** — `GET /api/locations?type=`, `POST`, `PUT /{id}`, `DELETE /{id}`
- Delete es soft (marca `active = false`)
- Filtro `?type=warehouse|branch` retorna solo activas

### B3 — Items y Unit Conversions ✅
**Items** — `GET /api/items?active=true`, `POST`, `PUT /{id}`, `DELETE /{id}`
- Delete es soft (marca `active = false`)
- Response incluye `baseUnitName` y `baseUnitSymbol`

**Unit Conversions** (bajo `/api/items/{itemId}/conversions`):
- `GET /{itemId}/conversions`
- `POST /{itemId}/conversions` — valida duplicados (UNIQUE item+fromUnit)
- `DELETE /{itemId}/conversions/{id}`

### B4 — Inventory Stock ✅
**`GET /api/inventory/stock`**
- Respuesta agrupada por item: `ItemStockResponse` con lista de `StockLocationEntry` por location
- Flag `lowStock: true` cuando `quantity < minQuantity`
- Campo `totalQuantity` suma todas las locations

### B5 — Movimientos de inventario ✅
**`POST /api/inventory/purchase`**
- Convierte a unidad base, suma stock en bodega (primera warehouse activa)

**`POST /api/inventory/transfer`**
- Valida stock suficiente en bodega, descuenta bodega y suma sucursal destino
- Transacción atómica

**`POST /api/inventory/consumption`**
- Descuenta de la location indicada, valida stock suficiente

**`POST /api/inventory/adjustment`**
- Quantity puede ser negativo, valida que no resulte en stock negativo
- Notes obligatorio

**`GET /api/inventory/movements?type=&itemId=&locationId=&from=&to=`**
- Historial con filtros opcionales, orden desc por fecha

### B6 — Transfer Requests ✅
**`POST /api/inventory/transfer-request`** — crea solicitud en estado `pending`

**`GET /api/inventory/requests`** — lista todas las solicitudes `pending`

**`POST /api/inventory/requests/{id}/complete`**
- Ejecuta el transfer real via `InventoryMovementService.transfer()`
- Marca la solicitud como `completed`

**`POST /api/inventory/requests/{id}/reject`**
- Body opcional: `{ "notes": "..." }`
- Marca la solicitud como `rejected`

---

## Estructura de archivos creados

### Models
| Archivo | Tabla |
|---|---|
| `model/Unit.java` | `units` |
| `model/Item.java` | `items` |
| `model/UnitConversion.java` | `unit_conversions` |
| `model/Location.java` | `locations` |
| `model/InventoryStock.java` | `inventory_stock` |
| `model/InventoryMovement.java` | `inventory_movements` |
| `model/TransferRequest.java` | `transfer_requests` |

### Repositories
| Archivo | Notas |
|---|---|
| `repository/UnitRepository.java` | — |
| `repository/ItemRepository.java` | `findByActiveTrue()` |
| `repository/UnitConversionRepository.java` | `existsByFromUnitIdOrToUnitId`, `findByItemId` |
| `repository/LocationRepository.java` | `findByActiveTrue`, `findByTypeAndActiveTrue` |
| `repository/InventoryStockRepository.java` | `findByItemIdAndLocationId` (upsert) |
| `repository/InventoryMovementRepository.java` | + `JpaSpecificationExecutor` |
| `repository/TransferRequestRepository.java` | `findByStatusOrderByCreatedAtDesc` |

### DTOs
| Request | Response |
|---|---|
| `UnitRequest` | `UnitResponse` |
| `ItemRequest` | `ItemResponse` |
| `UnitConversionRequest` | `UnitConversionResponse` |
| `LocationRequest` | `LocationResponse` |
| `PurchaseRequest` | `InventoryMovementResponse` |
| `TransferRequestDto` | `ItemStockResponse` + `StockLocationEntry` |
| `ConsumptionRequest` | `TransferRequestResponse` |
| `AdjustmentRequest` | — |
| `TransferRequestCreateDto` | — |
| `RejectRequestDto` | — |

### Services / Controllers
| Service | Controller |
|---|---|
| `UnitService` | `UnitController` → `/api/units` |
| `LocationService` | `LocationController` → `/api/locations` |
| `ItemService` | `ItemController` → `/api/items` |
| `InventoryStockService` | `InventoryController` → `/api/inventory` |
| `InventoryMovementService` | (mismo controller) |
| `TransferRequestService` | `TransferRequestController` → `/api/inventory` |

---

## Notas importantes

- El DTO de transfer se llama `TransferRequestDto` (no `TransferRequest`) para evitar conflicto con la entity `model/TransferRequest.java`
- La conversión de unidades ocurre en `InventoryMovementService.convertToBase()` — mismo método replicado en `TransferRequestService`
- `findWarehouse()` retorna la primera location activa de tipo `warehouse` — si hay múltiples bodegas se debe ajustar para pasar `locationFromId` en purchase
- `InventoryStockService.save()` es público para que `InventoryMovementService` pueda hacer upsert
- Sin autenticación por ahora — los endpoints de `encargado_sucursal` reciben `locationId` en el body (se cambiará cuando auth esté lista)
