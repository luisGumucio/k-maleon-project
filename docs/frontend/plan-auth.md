# Frontend Plan — Autenticación y Roles (Refine + Ant Design)

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso
> Referencia: `docs/features/06-roles-inventory.md`
> Prerequisito: Backend `plan-auth.md` Fases 1–4 completadas antes de iniciar aquí.

---

## FASE 1 — authProvider de Refine ✅

**Objetivo:** Implementar autenticación real reemplazando el mock de `VITE_MOCK_ROLE`.

- [x] Crear `src/providers/auth.ts` con el `authProvider` de Refine:
  - [x] `login({ email, password })` → `POST /api/auth/login` → guarda `token` y `role` en `localStorage`
  - [x] `logout()` → `POST /api/auth/logout` → limpia `localStorage`
  - [x] `checkAuth()` → verifica token + llama `GET /api/auth/me`
  - [x] `getPermissions()` → retorna el `role`
  - [x] `getIdentity()` → retorna `{ name, role }`
  - [x] `onError({ status })` → si 401 o 403, redirige a `/login`
- [x] `dataProvider` incluye header `Authorization: Bearer <token>` en todos los requests

---

## FASE 2 — Pantalla de Login ✅

**Objetivo:** UI de login conectada al authProvider.

- [x] `src/pages/login/index.tsx` — formulario email/contraseña con error display
- [x] Llamada directa a `authProvider.login()` (sin `useLogin` de Refine — evita QueryClient fuera de contexto)
- [x] Redirección con `window.location.href = "/"` post-login

---

## FASE 3 — Layouts por rol real ✅

**Objetivo:** Reemplazar `VITE_MOCK_ROLE` por el rol real del `authProvider`.

- [x] `src/contexts/role/index.tsx` lee el rol desde `localStorage` (puesto por authProvider)
- [x] `VITE_MOCK_ROLE` eliminado de la lógica, marcado como deprecado en `.env.example`
- [x] `authProvider` pasado a todas las instancias de `<Refine>` en los layouts

---

## FASE 4 — Gestión de usuarios (página `/users`) ✅

**Objetivo:** UI para que `super_admin` e `inventory_admin` gestionen usuarios.

- [x] `src/pages/users/list.tsx` — tabla conectada a `GET /api/users`
- [x] Crear usuario → `POST /api/users` con nombre, email, contraseña, rol
- [x] `inventory_admin` no ve selector de rol (siempre crea `almacenero`)
- [x] Al crear `encargado_sucursal` aparece campo de sucursal
- [x] Editar → `PUT /api/users/:id` (nombre + contraseña opcional)
- [x] Eliminar → `DELETE /api/users/:id` con Popconfirm

---

## FASE 5 — Protección de rutas y limpieza ✅

**Objetivo:** Asegurar que ninguna ruta sea accesible sin autenticación.

- [x] `App.tsx` verifica token en localStorage antes de renderizar cualquier layout
- [x] Sin token → renderiza `<LoginPage />` directamente (no `<Refine>`)
- [x] `VITE_MOCK_ROLE` eliminado del código (solo comentario en `.env.example`)
- [x] `Authorization` header en todos los requests del `dataProvider`

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5
```

> Todas las fases completadas.
