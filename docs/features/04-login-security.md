# Feature: Layouts de UI por Rol — K-Maleon

## Contexto

Antes de implementar el login, se construyen las tres vistas principales del
sistema. Por ahora el rol activo se hardcodea con `VITE_MOCK_ROLE` para poder
desarrollar y revisar cada layout de forma independiente. Cuando el feature de
autenticación esté listo, el mock se reemplaza por el rol que devuelva el
`authProvider`.

---

## Estructura general

El punto de entrada de la app lee el rol activo y renderiza el layout
correspondiente:

```
rol === 'super_admin'     → Layout SuperAdmin
rol === 'admin'           → Layout Admin
rol === 'inventory_admin' → Layout InventoryAdmin
```

---

## Layout 1 — SuperAdmin

### Acceso
Rol `super_admin`. Tiene su propio menú lateral con tres ítems fijos. Desde el
dashboard puede entrar a los paneles de los otros roles.

### Menú lateral

- **Dashboard** → `/super/dashboard`
- **Usuarios** → `/users`
- **Configuraciones** → `/settings`

### Pantalla Dashboard SuperAdmin (`/super/dashboard`)

Una pantalla con:

- **Dos tarjetas/botones grandes** para navegar a los paneles:
  - "Panel de Transacciones" → al hacer clic carga el layout de Admin completo.
  - "Panel de Inventario" → al hacer clic carga el layout de InventoryAdmin
    completo.

### Página Usuarios (`/users`)
CRUD completo de usuarios. Puede crear cualquier rol (super_admin, admin,
inventory_admin, almacenero).

### Página Configuraciones (`/settings`)
Placeholder por ahora con el texto "Configuraciones — próximamente". Aquí irán
las reglas de negocio cuando estén definidas.

### Comportamiento de navegación hacia otros paneles

Cuando el super_admin entra a "Panel de Transacciones" desde el dashboard, ve
exactamente el mismo layout y menú que el rol `admin`. Cuando entra a "Panel de
Inventario", ve el mismo layout y menú que el rol `inventory_admin`.

En ambos casos debe haber un botón visible para **volver al Dashboard SuperAdmin**
(en el header o al inicio del menú lateral del panel que esté visitando).

### Notas de implementación

El super_admin no duplica las páginas de los otros layouts. Monta el componente
de layout de Admin o InventoryAdmin pasando un parámetro que indica que el origen
es super_admin, para mostrar el botón de regreso.

---

## Layout 2 — Admin

### Acceso
Rol `admin`. Módulo de transacciones completo.

### Menú lateral

- **Dashboard** → `/admin/dashboard` — resumen de saldo de cuenta y operaciones
  activas.
- **Cuenta** → `/account`
- **Proveedores** → `/suppliers`
- **Operaciones** → `/operations`
- **Movimientos** → `/movements`
- **Shipments** → `/shipments`
- **Audit Log** → `/audit-log`

---

## Layout 3 — InventoryAdmin

### Acceso
Rol `inventory_admin`. Módulo de inventario + gestión de almaceneros.

### Menú lateral

- **Dashboard** → `/inventory/dashboard` — resumen de inventario (stock total,
  sucursales activas).
- **Bodegas** → `/warehouses`
- **Sucursales** → `/branches`
- **Stock** → `/stock`
- **Usuarios** → `/users`

### Páginas del módulo de inventario

Estas páginas van con datos mockeados o vacíos por ahora. El objetivo es tener
la UI lista para conectar con el backend cuando se implemente el módulo.

**Bodegas (`/warehouses`)**
Tabla con columnas: Nombre, Ubicación, Acciones (editar, eliminar). Botón para
crear nueva bodega. Formulario modal con campos: nombre y ubicación.

**Sucursales (`/branches`)**
Tabla con columnas: Nombre, Dirección, Acciones. Botón para crear nueva sucursal.
Formulario modal con campos: nombre y dirección.

**Stock (`/stock`)**
Tabla con columnas: Producto, Bodega/Sucursal, Cantidad disponible. Filtro por
bodega o sucursal. Solo lectura, sin formulario de edición por ahora.

**Usuarios (`/users`)**
Tabla con columnas: Nombre, Email, Rol, Fecha de creación, Acciones (editar,
eliminar). Botón para crear usuario. Formulario modal con campos: nombre, email,
contraseña. El rol no es seleccionable (siempre `almacenero`).

---

## Implementación técnica en Refine

### Rol hardcodeado (temporal)

Definir `VITE_MOCK_ROLE` en el `.env`. El componente raíz lee ese valor y
renderiza el layout correspondiente. Al implementar auth, este mock se elimina y
se reemplaza por `authProvider.getPermissions()`.

```
VITE_MOCK_ROLE=super_admin   # para probar el dashboard central
VITE_MOCK_ROLE=admin         # para probar el panel de transacciones
VITE_MOCK_ROLE=inventory_admin  # para probar el panel de inventario
```

### Separación de recursos por layout

Cada layout registra únicamente sus propias rutas en Refine. Las rutas de un
layout no son accesibles directamente desde otro, excepto por la navegación
controlada del super_admin.

### Componentes compartidos

El header y el sider son los únicos componentes que cambian entre layouts. El
resto de la UI reutiliza los mismos componentes de Ant Design ya existentes en
el proyecto.

---

## Notas importantes

- No implementar lógica de permisos real todavía, solo el enrutamiento visual.
- La página `/settings` es un placeholder vacío por ahora.
- El super_admin no tiene menú lateral propio, su punto de entrada siempre es el
  dashboard con las dos tarjetas de acceso.
- El layout de `almacenero` se implementa en una iteración posterior.
- Todo debe seguir los mismos patrones de componentes, estructura de carpetas y
  convenciones que ya existen en el proyecto.