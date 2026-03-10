# Plan Backend: Autorización por Rol — Módulo de Inventario

## Estado: COMPLETADO

---

## Problema

Los endpoints del módulo de inventario no tienen restricción de rol. Cualquier usuario
autenticado (incluido un `admin` de transacciones) puede llamar a `POST /api/inventory/purchase`,
ver todo el stock, o completar solicitudes de transferencia.

Según el documento de features (`docs/features/06-roles-inventory.md`), cada rol tiene
un alcance definido:

| Rol | Puede hacer |
|---|---|
| `inventory_admin` | Todo — gestionar items, unidades, ubicaciones, compras, transferencias, ajustes, solicitudes, historial |
| `almacenero` | Registrar compras, transferir stock, ver solicitudes y completarlas/rechazarlas, ver historial |
| `encargado_sucursal` | Registrar consumos, solicitar transferencias, ver el stock y el historial **de su sucursal** |

Además, los endpoints de configuración (`/api/items`, `/api/units`, `/api/locations`) y
los de ajuste/configuración de stock mínimo solo deben ser accesibles para `inventory_admin`.

---

## Constantes de rol existentes (`Roles.java`)

```java
INVENTORY_ADMIN = "ROLE_INVENTORY_ADMIN"
ALMACENERO      = "ROLE_ALMACENERO"
ENCARGADO       = "ROLE_ENCARGADO_SUCURSAL"
```

El `JwtFilter` ya construye el `GrantedAuthority` como `"ROLE_" + profile.getRole().toUpperCase()`,
por lo que `@PreAuthorize` funciona correctamente con estos valores.

El `AuthenticatedUser` inyectado en el `SecurityContext` tiene: `userId`, `role`, `name`.
Para el `encargado_sucursal` también necesitamos `locationId` — ver F3.

---

## Fases

### F1 — Agregar `@PreAuthorize` a `InventoryController` ✅

Agregar en `Roles.java` las constantes que faltan:

```java
public static final String INVENTORY_STAFF =
    "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ALMACENERO','ENCARGADO_SUCURSAL')";

public static final String INVENTORY_MANAGERS =
    "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN')";

public static final String ALMACENERO_OR_ADMIN =
    "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ALMACENERO')";

public static final String ENCARGADO_OR_ADMIN =
    "hasAnyRole('SUPER_ADMIN','INVENTORY_ADMIN','ENCARGADO_SUCURSAL')";
```

Aplicar `@PreAuthorize` en `InventoryController`:

| Endpoint | Roles permitidos | Constante |
|---|---|---|
| `GET /inventory/stock` | todos los de inventario | `INVENTORY_STAFF` |
| `POST /inventory/purchase` | `inventory_admin`, `almacenero` | `ALMACENERO_OR_ADMIN` |
| `POST /inventory/transfer` | `inventory_admin`, `almacenero` | `ALMACENERO_OR_ADMIN` |
| `POST /inventory/consumption` | `inventory_admin`, `encargado_sucursal` | `ENCARGADO_OR_ADMIN` |
| `POST /inventory/adjustment` | solo `inventory_admin` | `INVENTORY_MANAGERS` |
| `GET /inventory/movements` | todos los de inventario | `INVENTORY_STAFF` |
| `PUT /inventory/items/{id}/min-quantity` | solo `inventory_admin` | `INVENTORY_MANAGERS` |

---

### F2 — Agregar `@PreAuthorize` a `TransferRequestController` ✅

| Endpoint | Roles permitidos | Constante |
|---|---|---|
| `GET /inventory/transfer-requests` | todos los de inventario | `INVENTORY_STAFF` |
| `POST /inventory/transfer-requests` | `inventory_admin`, `encargado_sucursal` | `ENCARGADO_OR_ADMIN` |
| `POST /inventory/transfer-requests/{id}/complete` | `inventory_admin`, `almacenero` | `ALMACENERO_OR_ADMIN` |
| `POST /inventory/transfer-requests/{id}/reject` | `inventory_admin`, `almacenero` | `ALMACENERO_OR_ADMIN` |

---

### F3 — Agregar `locationId` al `AuthenticatedUser` ✅

Actualmente `AuthenticatedUser` tiene `userId`, `role`, `name`.
El `encargado_sucursal` tiene asignado un `location_id` en `user_profiles`.

**Cambio en `JwtFilter`:**
- Al cargar el `UserProfile`, leer también `profile.getLocationId()`.
- Incluirlo en `AuthenticatedUser`.

**Cambio en `AuthenticatedUser`:**
- Agregar campo `locationId` (nullable `UUID`).

Esto permite que los servicios lean el `locationId` del usuario autenticado
para filtrar datos sin que el cliente lo envíe.

---

### F4 — Filtrar stock y movimientos por `locationId` para `encargado_sucursal` ✅

**`InventoryStockService.findAll()`:**
- Si el caller es `encargado_sucursal`, filtrar `ItemStockResponse.locations`
  para devolver solo la entrada de su `locationId`.
- Si no tiene stock en su ubicación, el item no aparece.

**`InventoryMovementService.findAll()`:**
- Si el caller es `encargado_sucursal`, forzar `locationId = caller.locationId`
  independientemente del parámetro recibido.

**`TransferRequestService.findAll()`:**
- Si el caller es `encargado_sucursal`, filtrar por `locationId = caller.locationId`.
- Si el caller es `almacenero` o `inventory_admin`, ve todas.

El patrón a usar: recibir el `AuthenticatedUser` como parámetro en los métodos de servicio
(igual que `AccountService` recibe `callerId`).

---

### F5 — Proteger `ItemController`, `UnitController`, `LocationController` ✅

Estos endpoints de configuración solo deben ser accesibles para `inventory_admin`
(y `super_admin`). El `almacenero` y `encargado_sucursal` solo necesitan leer.

| Controlador | Método | Roles permitidos |
|---|---|---|
| `ItemController` | `GET` | `INVENTORY_STAFF` |
| `ItemController` | `POST`, `PUT`, `DELETE` | `INVENTORY_MANAGERS` |
| `UnitController` | `GET` | `INVENTORY_STAFF` |
| `UnitController` | `POST`, `PUT`, `DELETE` | `INVENTORY_MANAGERS` |
| `LocationController` | `GET` | `INVENTORY_STAFF` |
| `LocationController` | `POST`, `PUT`, `DELETE` | `INVENTORY_MANAGERS` |

Usar `@PreAuthorize` a nivel de método con la constante correspondiente.

---

### F6 — Proteger endpoints de conversiones de unidades ✅ (incluido en F5 — ItemController)

`GET /api/items/{id}/conversions` → `INVENTORY_STAFF` (todos lo leen)
`POST /api/items/{id}/conversions` → `INVENTORY_MANAGERS` (solo admin)
`DELETE /api/items/{id}/conversions/{convId}` → `INVENTORY_MANAGERS`

---

## Archivos a modificar

| Archivo | Cambio |
|---|---|
| `security/Roles.java` | Agregar constantes `INVENTORY_STAFF`, `INVENTORY_MANAGERS`, `ALMACENERO_OR_ADMIN`, `ENCARGADO_OR_ADMIN` |
| `security/AuthenticatedUser.java` | Agregar campo `locationId` (UUID, nullable) |
| `security/JwtFilter.java` | Leer `profile.getLocationId()` y pasarlo a `AuthenticatedUser` |
| `controller/InventoryController.java` | Agregar `@PreAuthorize` por endpoint |
| `controller/TransferRequestController.java` | Agregar `@PreAuthorize` por endpoint |
| `controller/ItemController.java` | Agregar `@PreAuthorize` por método |
| `controller/UnitController.java` | Agregar `@PreAuthorize` por método |
| `controller/LocationController.java` | Agregar `@PreAuthorize` por método |
| `service/InventoryStockService.java` | Filtrar por locationId si rol es `encargado_sucursal` |
| `service/InventoryMovementService.java` | Forzar locationId si rol es `encargado_sucursal` |
| `service/TransferRequestService.java` | Filtrar por locationId si rol es `encargado_sucursal` |

---

## Orden recomendado de implementación

F1 → F2 → F3 → F4 → F5 → F6

F1 y F2 son cambios simples de una línea por endpoint.
F3 desbloquea F4 (sin locationId en el user, no se puede filtrar).
F5 y F6 son independientes y se pueden hacer en cualquier momento.
