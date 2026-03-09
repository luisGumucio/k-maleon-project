# Feature: Shipments — Rastreo de Contenedores

## Contexto

Anteriormente los envíos de contenedores se registraban manualmente en un folder
físico. Este feature digitaliza ese proceso: permite registrar cada envío,
asociarlo a un proveedor, y adjuntar el comprobante (factura, BL, etc.) usando
el endpoint de attachments ya existente en el proyecto.

---

## Base de Datos

Crear la tabla `shipments` con la siguiente estructura:

```sql
CREATE TABLE shipments (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  number           INTEGER GENERATED ALWAYS AS IDENTITY,
  supplier_id      UUID NOT NULL REFERENCES suppliers(id),
  departure_date   DATE,
  container_number TEXT,
  quantity         INTEGER,
  product_details  TEXT,
  arrival_date     DATE,
  document_url     TEXT,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

El campo `number` es un correlativo visible para el usuario (entero autoincrmental),
no editable. `document_url` almacena la URL pública devuelta por el endpoint de
attachments tras el upload.

---

## Backend — Spring Boot

Seguir exactamente los mismos patrones, estructura de paquetes y convenciones que
ya existen en el proyecto. No inventar nuevas convenciones. El prefijo de rutas es
`/api/` tal como el resto de los endpoints.

### Endpoints a crear

- `GET /api/shipments` — listado. Soportar filtro opcional por `supplier_id`. La
  respuesta debe incluir el nombre del proveedor (join con `suppliers`) además del ID.
- `GET /api/shipments/{id}` — detalle de un envío.
- `POST /api/shipments` — crear un nuevo envío. El body incluye todos los campos
  editables pero NO `document_url` ni `number`.
- `PUT /api/shipments/{id}` — actualizar los campos editables de un envío,
  incluyendo `document_url` (que se setea desde el frontend después del upload).
- `DELETE /api/shipments/{id}` — eliminar un envío.

### Lo que NO hay que crear

El upload de comprobantes **no necesita un endpoint propio** para shipments. Ya
existe `POST /api/attachments/upload` que recibe el archivo, lo sube a Supabase
Storage y retorna la URL pública. El frontend usa ese endpoint y luego persiste la
URL en el shipment vía `PUT /api/shipments/{id}`.

---

## Frontend — Refine + Ant Design

Seguir exactamente los mismos patrones de estructura, hooks de Refine y componentes
de Ant Design que ya existen en el proyecto para otras entidades.

### Qué implementar

**Lista (`ShipmentList`)**
Tabla con columnas: N° (number), Proveedor, N° Contenedor, Fecha Partida, Fecha
Llegada, Cantidad, y una columna de acciones (ver, editar, eliminar). Soportar
filtro por proveedor mediante un selector en la parte superior.

**Formulario de creación y edición (`ShipmentForm`)**
Campos: proveedor (selector con búsqueda que carga de `/api/suppliers`), fecha de
partida, N° de contenedor, cantidad, detalles del producto (textarea), fecha de
llegada. El campo N° es solo lectura y no aparece en el formulario de creación.

**Subida de documento**
Dentro del formulario de edición o en el detalle del shipment, mostrar una sección
para el comprobante con el siguiente flujo:

1. El usuario selecciona un archivo (PDF o imagen, máx 10MB, igual que el resto de
   attachments del proyecto).
2. El frontend llama a `POST /api/attachments/upload` con el archivo.
3. La respuesta retorna `{ "url": "..." }`.
4. Esa URL se guarda automáticamente en el shipment vía `PUT /api/shipments/{id}`
   actualizando el campo `document_url`.
5. Si ya existía un documento, simplemente se sobreescribe la URL (el archivo
   anterior en Storage queda huérfano; no es necesario borrarlo por ahora).

Si el shipment ya tiene `document_url`, mostrar un enlace/botón para abrir o
descargar el comprobante, y permitir reemplazarlo con el mismo flujo.

**Detalle (`ShipmentShow`)**
Mostrar todos los campos del envío. Si tiene `document_url`, mostrar un
botón/enlace para abrir o descargar el comprobante.

---

## Notas importantes

- Respetar el manejo de errores, validaciones y respuestas de error que ya usa el
  proyecto.
- No agregar librerías nuevas si la funcionalidad está cubierta por las dependencias
  existentes.
- El bucket de Storage que corresponde a shipments es `container-docs` (diferente
  al bucket `financial-docs` que usa attachments actualmente). Revisar si el
  endpoint `/api/attachments/upload` permite parametrizar el bucket destino o si
  hay que extenderlo para soportar un bucket adicional.