# BUG-002 — 401 al subir archivo en `POST /api/attachments/upload`

## Estado: RESUELTO (dos fixes — frontend + backend)

## Síntoma

Al intentar subir un archivo desde el frontend, el endpoint devuelve:

```json
{"status":401,"error":"Unauthorized","message":"Token requerido"}
```

Endpoint: `POST http://localhost:8080/api/attachments/upload`

## Causa

El backend está correctamente protegido — el error es del frontend. El upload de archivo
(multipart/form-data) se hace fuera del `dataProvider` de Refine, usando un `fetch` o
`axios` directo que **no incluye el header `Authorization: Bearer <token>`**.

## Solución

En el código de upload (probablemente `MovementCreateModal.tsx` o similar), asegurarse
de leer el token del localStorage y añadirlo al header:

```ts
const token = localStorage.getItem("auth_token"); // o la key que use el authProvider

const formData = new FormData();
formData.append("file", file);

const res = await fetch(`${apiUrl}/api/attachments/upload`, {
  method: "POST",
  headers: {
    Authorization: `Bearer ${token}`,
    // NO poner Content-Type — fetch lo setea automáticamente con el boundary correcto
  },
  body: formData,
});
```

## Archivos relevantes

- `frontend/src/pages/movements/MovementCreateModal.tsx` — donde se hace el upload
- `frontend/src/providers/auth.ts` — donde se guarda el token en localStorage
- `backend/src/main/java/com/kmaleon/controller/AttachmentController.java` — correcto, no tocar
- `backend/src/main/java/com/kmaleon/config/SecurityConfig.java` — correcto, no tocar
