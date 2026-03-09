# Feature: Registro de Contenedores de Importación

## Objetivo

Permitir registrar **contenedores de importación** y los **productos que contiene cada contenedor**, similar a la estructura del Excel utilizado actualmente.

El sistema debe permitir:

* Registrar un contenedor
* Registrar los productos dentro del contenedor
* Visualizar lista de contenedores
* Ver detalle del contenedor con sus productos

---

# 1. Estructura de Base de Datos

## Tabla: contenedores

Guarda la información general del contenedor.

| campo             | tipo          | descripción                 |
| ----------------- | ------------- | --------------------------- |
| id                | uuid / bigint | identificador               |
| numero_contenedor | text          | número del contenedor       |
| proveedor         | text          | proveedor del contenedor    |
| fecha_partida     | date          | fecha de salida             |
| fecha_llegada     | date          | fecha de llegada            |
| total_items       | integer       | cantidad total de registros |
| total_contenedor  | numeric       | costo total del contenedor  |
| created_at        | timestamp     | fecha de creación           |

---

## Tabla: contenedor_detalle

Guarda los productos que vienen dentro del contenedor.

| campo           | tipo          | descripción              |
| --------------- | ------------- | ------------------------ |
| id              | uuid / bigint | identificador            |
| contenedor_id   | uuid / bigint | referencia al contenedor |
| descripcion     | text          | descripción del producto |
| cantidad        | integer       | cantidad                 |
| precio_unitario | numeric       | precio por unidad        |
| importe         | numeric       | cantidad * precio        |
| created_at      | timestamp     | fecha de registro        |

---

## Relación

Un contenedor puede tener muchos productos.

contenedores (1) → (N) contenedor_detalle

```
contenedores
      │
      │ id
      │
contenedor_detalle
```

---

# 2. Flujo del Sistema

## Paso 1: Crear contenedor

Formulario:

* numero_contenedor
* proveedor
* fecha_partida
* fecha_llegada

Guardar registro en:

```
contenedores
```

---

## Paso 2: Agregar productos al contenedor

Formulario tipo tabla dinámica:

| Cantidad | Descripción | Precio | Importe |
| -------- | ----------- | ------ | ------- |

Campos:

* cantidad
* descripcion
* precio_unitario

El sistema calcula:

```
importe = cantidad * precio_unitario
```

Se guardan registros en:

```
contenedor_detalle
```

---

# 3. Vista en el Sistema

## Lista de contenedores

Tabla:

| ID | Número contenedor | Proveedor | Fecha partida | Fecha llegada | Acción      |
| -- | ----------------- | --------- | ------------- | ------------- | ----------- |
| 1  | CARU5170029       | Chino     | 2026-02       | 2026-03       | Ver detalle |

Acciones:

* Ver detalle
* Editar
* Eliminar

---

## Vista detalle del contenedor

Información general:

```
Contenedor: CARU5170029
Proveedor: Chino
Fecha partida: 2026-02
Fecha llegada: 2026-03
```

Tabla de productos:

| Cantidad | Descripción    | Precio | Importe |
| -------- | -------------- | ------ | ------- |
| 100      | Converse mujer | 5      | 500     |
| 80       | Asia mujer     | 6      | 480     |
| 50       | Sandalia mujer | 4      | 200     |

---

# 4. Cálculos Automáticos

Al guardar:

```
importe = cantidad * precio_unitario
```

Total contenedor:

```
total_contenedor = SUM(importe)
```

Total items:

```
total_items = COUNT(productos)
```

Estos valores se guardan en la tabla:

```
contenedores
```

---

# 5. Integración con Refine

Se crearán los siguientes resources:

```
contenedores
contenedor_detalle
```

Pantallas necesarias:

1. ListContenedores
2. CreateContenedor
3. ShowContenedor
4. EditContenedor

---

# 6. Posible mejora futura

Opcionalmente se puede integrar con inventario para:

* aumentar stock automáticamente
* calcular costo promedio
* relacionar productos con catálogo del sistema

---

# 7. Resultado esperado

El sistema permitirá registrar contenedores y visualizar los productos de forma estructurada similar al Excel actual, facilitando el control de importaciones.
