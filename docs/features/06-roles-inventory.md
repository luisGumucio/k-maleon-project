# Feature: Autenticación y Roles — K-Maleon

## Contexto

El sistema actualmente no tiene seguridad. Este feature agrega login y un sistema
de roles que controla qué puede ver y hacer cada usuario. El frontend solo conoce
el backend, Supabase es un detalle de implementación interno.

---

## Flujo de autenticación

```
Frontend  -->  POST /api/auth/login (email + password)
                      |
               Backend llama a Supabase Auth REST API
                      |
               Supabase devuelve JWT
                      |
Frontend  <--  { token, role, name }

-- En cada request posterior --

Frontend  -->  GET /api/operaciones  (Authorization: Bearer JWT)
                      |
               Backend valida firma del JWT con el JWT secret de Supabase
               Backend extrae user_id y consulta user_profiles para obtener el rol
               Backend decide si permite o rechaza
                      |
Frontend  <--  200 OK / 403 Forbidden
```

El frontend nunca llama a Supabase directamente. No tiene la URL de Supabase ni
ninguna key. Solo habla con el backend.

---

## Roles definidos

| Rol               | Descripción                                                                 |
|-------------------|-----------------------------------------------------------------------------|
| `super_admin`     | Acceso total al sistema. Ve y opera todo sin restricciones.                 |
| `admin`           | Acceso al módulo de transacciones: operaciones, movimientos, proveedores,   |
|                   | cuenta, shipments, attachments, audit log.                                  |
| `inventory_admin` | Acceso al módulo de inventario. Puede crear y gestionar usuarios con rol    |
|                   | `almacenero` y `encargado_sucursal`.                                        |
| `almacenero`      | Opera en bodega central. Registra compras y transfiere stock a sucursales.  |
|                   | Creado por un `inventory_admin`.                                            |
| `encargado_sucursal` | Asignado a una sucursal específica. Registra consumos, ve el stock de   |
|                   | su sucursal, ve el historial de su sucursal y solicita transferencias desde  |
|                   | bodega. Solo ve datos de la sucursal que tiene asignada.                    |

---

## Base de datos

Los usuarios viven en `auth.users` de Supabase. El rol se guarda en una tabla
propia del proyecto:

```sql
CREATE TABLE user_profiles (
  id         UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  role       TEXT NOT NULL CHECK (role IN ('super_admin', 'admin', 'inventory_admin', 'almacenero', 'encargado_sucursal')),
  name       TEXT,
  location_id UUID REFERENCES locations(id), -- solo para encargado_sucursal, null para los demás
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

El `id` coincide con el `user_id` del JWT de Supabase.

---

## Backend — Spring Boot

Seguir los mismos patrones y estructura de paquetes del proyecto.

### Nuevos endpoints de autenticación

**`POST /api/auth/login`** — público, no requiere JWT.
- Recibe `{ email, password }`.
- Llama a la Supabase Auth REST API (`/auth/v1/token?grant_type=password`) con las
  credenciales usando el `service role key`.
- Supabase devuelve el JWT.
- El backend consulta `user_profiles` con el `user_id` extraído del JWT para
  obtener el rol y el nombre.
- Retorna `{ token, role, name }` al frontend.

**`POST /api/auth/logout`** — requiere JWT.
- Llama a Supabase Auth para invalidar la sesión.
- Retorna 200 OK.

**`GET /api/auth/me`** — requiere JWT.
- Retorna el perfil del usuario autenticado: `{ id, name, role }`.
- Útil para que el frontend rehidrate la sesión al refrescar la página.

### Validación del JWT en cada request

- Configurar Spring Security para interceptar todos los requests bajo `/api/**`
  excepto `/api/auth/login`.
- Extraer el JWT del header `Authorization: Bearer <token>`.
- Verificar la firma del token usando el **JWT secret de Supabase** (variable de
  entorno, ya disponible en el proyecto).
- Extraer el `sub` (user_id) del token y consultar `user_profiles` para obtener
  el rol.
- Inyectar el usuario autenticado en el contexto de Spring Security para que los
  controllers puedan acceder al rol.

### Protección de endpoints por rol

| Endpoints                  | Roles permitidos                                    |
|----------------------------|-----------------------------------------------------|
| `POST /api/auth/login`     | Público                                             |
| `/api/account/**`          | `super_admin`, `admin`                              |
| `/api/suppliers/**`        | `super_admin`, `admin`                              |
| `/api/operations/**`       | `super_admin`, `admin`                              |
| `/api/movements/**`        | `super_admin`, `admin`                              |
| `/api/attachments/**`      | `super_admin`, `admin`                              |
| `/api/shipments/**`        | `super_admin`, `admin`                              |
| `/api/audit-log/**`        | `super_admin`, `admin`                              |
| `GET /api/users`           | `super_admin`, `inventory_admin`                    |
| `POST /api/users`          | `super_admin`, `inventory_admin`                    |
| `PUT /api/users/{id}`      | `super_admin`, `inventory_admin`                    |
| `DELETE /api/users/{id}`   | `super_admin`, `inventory_admin`                    |

### Nuevo módulo de gestión de usuarios `/api/users`

- `GET /api/users` — listar usuarios. `super_admin` ve todos; `inventory_admin`
  solo ve los `almacenero` que él creó.
- `POST /api/users` — crear usuario. El backend llama a Supabase Auth Admin API
  para crear el usuario con email y contraseña, luego inserta en `user_profiles`.
  `inventory_admin` puede crear roles `almacenero` y `encargado_sucursal`.
  Al crear un `encargado_sucursal` debe asignarle obligatoriamente una sucursal
  (location de tipo `branch`) que quedará en `user_profiles.location_id`.
  `super_admin` puede crear cualquier rol.
  crear cualquier rol.
- `PUT /api/users/{id}` — actualizar nombre o resetear contraseña vía Supabase
  Auth Admin API.
- `DELETE /api/users/{id}` — deshabilitar el usuario en Supabase Auth y eliminar
  su perfil.

---

## Frontend — Refine + Ant Design

Seguir los mismos patrones del proyecto. El frontend no importa ningún SDK de
Supabase. Toda comunicación es contra el backend.

### Login

- Pantalla de login con campos email y contraseña.
- Al submit llama a `POST /api/auth/login`.
- Guarda el `token` y el `role` retornados (en el `authProvider` de Refine o en
  un contexto global).
- El `token` se adjunta automáticamente en el header `Authorization` de todos los
  requests siguientes.

### authProvider de Refine

Implementar el `authProvider` de Refine usando únicamente el backend:

- `login` → llama a `POST /api/auth/login`, guarda token y rol.
- `logout` → llama a `POST /api/auth/logout`, limpia el token.
- `checkAuth` → verifica si hay token guardado; si hay, llama a `GET /api/auth/me`
  para validar que sigue activo.
- `getPermissions` → retorna el rol guardado.
- `getIdentity` → retorna nombre y rol del usuario.

### Layouts por rol

Implementar layouts distintos según el rol retornado por `getPermissions`:

**`super_admin`** — menú completo: Cuenta, Proveedores, Operaciones, Movimientos,
Shipments, Audit Log, Gestión de Usuarios.

**`admin`** — menú sin Gestión de Usuarios: Cuenta, Proveedores, Operaciones,
Movimientos, Shipments, Audit Log.

**`inventory_admin`** — menú de inventario + Gestión de Usuarios (solo para crear
y administrar almaceneros).

**`almacenero`** — menú reducido al área operativa de inventario que le
corresponda.

### Protección de rutas

Usar `accessControlProvider` de Refine para que cada ruta valide el rol antes de
renderizar. Si un usuario accede a una ruta sin permiso, redirigir a su dashboard
correspondiente.

### Pantalla de gestión de usuarios

- Accesible para `super_admin` e `inventory_admin`.
- Tabla con listado de usuarios (nombre, email, rol, fecha de creación).
- Botón para crear usuario: formulario con nombre, email, contraseña y rol.
  `inventory_admin` no ve el selector de rol (siempre crea `almacenero`).
- Opciones por fila: editar nombre/contraseña, eliminar.

---

## Notas importantes

- El primer usuario `super_admin` se crea manualmente desde el dashboard de
  Supabase Auth + insertando su registro en `user_profiles`. No hay endpoint de
  registro público.
- No hay recuperación de contraseña por ahora; se gestiona manualmente vía el
  dashboard de Supabase.
- Todos los endpoints existentes deben quedar protegidos sin cambiar su lógica de
  negocio, solo se agrega la capa de autorización encima.
- El frontend no debe tener ninguna dependencia ni variable de entorno relacionada
  con Supabase.