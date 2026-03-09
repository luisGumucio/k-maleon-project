# Backend Plan — Autenticación y Roles (Spring Boot)

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso
> Referencia: `docs/features/06-roles-inventory.md`

---

## FASE 1 — Base de datos

**Objetivo:** Crear la tabla `user_profiles` y la migración SQL.

### Tasks
- [x] Crear `docs/database/migrations/013_create_user_profiles.sql`
  ```sql
  CREATE TABLE user_profiles (
    id          UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    role        TEXT NOT NULL CHECK (role IN ('super_admin', 'admin', 'inventory_admin', 'almacenero', 'encargado_sucursal')),
    name        TEXT,
    location_id UUID REFERENCES locations(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
  );
  ```
- [ ] Correr migración en Supabase SQL Editor
- [ ] Crear manualmente el primer `super_admin` desde Supabase Auth dashboard + insertar en `user_profiles`

### Validación
- [ ] La tabla existe y acepta los roles definidos
- [ ] El constraint de `role` rechaza valores inválidos

---

## FASE 2 — Dependencias y configuración Spring Security

**Objetivo:** Agregar Spring Security al proyecto y configurar la estructura base.

### Tasks
- [x] Agregar dependencias en `build.gradle.kts`:
  - `spring-boot-starter-security`
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken, versión 0.12.x)
- [x] Agregar variables de entorno:
  - `SUPABASE_JWT_SECRET` — JWT secret de Supabase (Settings → API → JWT Secret)
- [x] Crear `SecurityConfig.java` — deshabilita CSRF, configura stateless session, define rutas públicas vs protegidas
- [x] Crear `JwtFilter.java` — intercepta requests, extrae y valida el JWT del header `Authorization: Bearer`
- [x] Crear `UserProfile.java` (entidad JPA) y `UserProfileRepository.java`

### Reglas
- `/api/auth/login` es el único endpoint público
- Todos los demás endpoints bajo `/api/**` requieren JWT válido
- El filtro extrae `sub` (user_id) del JWT y lo usa para consultar `user_profiles`

### Validación
- [ ] Un request sin token a `/api/suppliers` devuelve 401
- [ ] Un request con token válido pasa el filtro correctamente

---

## FASE 3 — Endpoints de autenticación

**Objetivo:** Implementar login, logout y me.

### Tasks

#### DTOs
- [ ] `LoginRequest.java` — `{ email, password }`
- [ ] `LoginResponse.java` — `{ token, role, name }`
- [ ] `UserProfileResponse.java` — `{ id, name, role }`

#### Service
- [ ] `AuthService.java`
  - [ ] `login(email, password)` — llama a Supabase Auth REST (`/auth/v1/token?grant_type=password`) con `service role key`, extrae JWT, consulta `user_profiles`, retorna `LoginResponse`
  - [ ] `logout(token)` — llama a Supabase Auth (`/auth/v1/logout`) para invalidar sesión
  - [ ] `me(userId)` — consulta `user_profiles` por `userId`, retorna `UserProfileResponse`

#### Controller
- [ ] `AuthController.java`
  - [ ] `POST /api/auth/login` — público
  - [ ] `POST /api/auth/logout` — requiere JWT
  - [ ] `GET /api/auth/me` — requiere JWT

### Validación
- [ ] `POST /api/auth/login` con credenciales válidas retorna `{ token, role, name }`
- [ ] `POST /api/auth/login` con credenciales inválidas retorna 401
- [ ] `GET /api/auth/me` con token válido retorna el perfil del usuario

---

## FASE 4 — Protección de endpoints por rol

**Objetivo:** Agregar autorización a todos los endpoints existentes.

### Tasks
- [x] Crear anotaciones o constantes de roles: `Roles.SUPER_ADMIN`, `Roles.ADMIN`, etc.
- [x] Agregar `@PreAuthorize` a cada controller existente según la tabla de permisos:

| Endpoints | Roles permitidos |
|---|---|
| `/api/account/**` | `super_admin`, `admin` |
| `/api/suppliers/**` | `super_admin`, `admin` |
| `/api/operations/**` | `super_admin`, `admin` |
| `/api/movements/**` | `super_admin`, `admin` |
| `/api/attachments/**` | `super_admin`, `admin` |
| `/api/shipments/**` | `super_admin`, `admin` |
| `/api/audit-log/**` | `super_admin`, `admin` |
| `/api/users/**` | `super_admin`, `inventory_admin` |

- [ ] Habilitar `@EnableMethodSecurity` en `SecurityConfig`
- [ ] Verificar que el rol queda disponible en `SecurityContext` para que `@PreAuthorize` funcione

### Validación
- [ ] Un `admin` logueado puede acceder a `/api/suppliers` — responde 200
- [ ] Un `inventory_admin` logueado intenta `/api/suppliers` — responde 403
- [ ] Un `super_admin` puede acceder a todo

---

## FASE 5 — Gestión de usuarios (`/api/users`)

**Objetivo:** CRUD de usuarios via Supabase Auth Admin API.

### Tasks

#### DTOs
- [ ] `UserCreateRequest.java` — `{ name, email, password, role, locationId? }`
- [ ] `UserUpdateRequest.java` — `{ name?, password? }`
- [ ] `UserResponse.java` — `{ id, name, email, role, locationId?, createdAt }`

#### Service
- [ ] `UserService.java`
  - [ ] `findAll(callerRole, callerId)` — `super_admin` ve todos; `inventory_admin` solo ve los que él creó
  - [ ] `create(request, callerRole)` — llama a Supabase Auth Admin API (`/auth/v1/admin/users`) para crear el usuario, luego inserta en `user_profiles`. `inventory_admin` solo puede crear `almacenero` y `encargado_sucursal`
  - [ ] `update(id, request)` — actualiza nombre en `user_profiles` y/o password via Supabase Admin API
  - [ ] `delete(id)` — deshabilita en Supabase Auth y elimina de `user_profiles`

#### Controller
- [ ] `UserController.java`
  - [ ] `GET /api/users`
  - [ ] `POST /api/users`
  - [ ] `PUT /api/users/{id}`
  - [ ] `DELETE /api/users/{id}`

### Reglas
- `inventory_admin` al crear `encargado_sucursal` debe proveer `locationId` obligatorio
- La Supabase Admin API requiere el `service role key` en el header `apikey`

### Validación
- [ ] `super_admin` puede crear usuarios de cualquier rol
- [ ] `inventory_admin` no puede crear un `admin` — devuelve 403
- [ ] `inventory_admin` ve solo sus almaceneros en la lista
- [ ] Eliminar usuario lo deshabilita en Supabase Auth

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5
```

> Prerequisito: el backend ya está deployado en Railway (Fase 8 de `plan.md` completada).
