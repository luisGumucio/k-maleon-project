# Frontend Plan — Depósitos de Saldo (Account Deposits)

> Feature: UI para agregar saldo a la cuenta y ver el historial de depósitos.
> Depende de: `docs/backend/plan-account-deposits.md` (Fases 1–5 completadas)
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## Contexto

La página de cuenta (`/account`) actualmente muestra el saldo actual y tiene un modal para configurar el saldo inicial. Hay que agregar:
1. Botón "Agregar saldo" → modal para registrar un depósito
2. Historial de depósitos debajo del resumen financiero
3. Resumen expandido (saldo + totalDeposits + entradas + salidas)

---

## FASE 1 — Tipos TypeScript

### Tasks
- [x] Crear/actualizar `src/types/account.ts`:
  ```ts
  export type AccountDeposit = {
    id: string;
    accountId: string;
    amount: number;       // centavos
    description: string | null;
    date: string;         // ISO date
    createdAt: string;    // ISO datetime
  };

  export type AccountSummary = {
    balance: number;
    totalDeposits: number;
    totalEntradas: number;
    totalSalidas: number;
    updatedAt: string;
  };
  ```

### Validación
- [ ] No hay errores de TypeScript

---

## FASE 2 — Modal "Agregar Saldo"

**Archivo:** `src/pages/account/DepositModal.tsx` (nuevo)

### Tasks
- [ ] Botón trigger: `<Button type="primary" icon={<PlusOutlined />}>Agregar saldo</Button>`
- [ ] Modal con `Form` de Ant Design:
  - **Monto** (`amount`): InputNumber en USD con 2 decimales, mínimo $0.01, requerido
    - Convertir a centavos al enviar (`dollarsToCents()`)
  - **Fecha** (`date`): DatePicker, default hoy, requerido
  - **Descripción** (`description`): Input text, opcional
- [ ] `useMutation` → `POST /api/account/deposit`
- [ ] Al éxito: cerrar modal + invalidar queries `["account-balance"]`, `["account-deposits"]`, `["account-summary"]`
- [ ] Mostrar error del API si falla

### Validación
- [ ] Depositar monto válido → modal cierra y saldo se actualiza en pantalla
- [ ] Campo vacío → validación de form activa antes de enviar

---

## FASE 3 — Historial de Depósitos

**Archivo:** `src/pages/account/DepositHistory.tsx` (nuevo)

### Tasks
- [ ] `useQuery` → `GET /api/account/deposits`
- [ ] Tabla Ant Design con columnas:
  - **Fecha** — `date` formateada `DD/MM/YYYY`
  - **Monto** — `formatUSD(amount)` en verde
  - **Descripción** — texto o `—`
  - **Registrado** — `createdAt` formateada `DD/MM/YYYY HH:mm` en gris secundario debajo de fecha (misma celda, mismo patrón que MovementTable)
- [ ] `pagination={false}` si el historial es corto, o paginación simple si crece
- [ ] Estado vacío: `<Empty description="Sin depósitos registrados" />`
- [ ] Loading skeleton mientras carga

### Mobile
- [ ] Cards en lugar de tabla (misma lógica que MovementTable mobile)
  - Línea 1: monto en verde
  - Línea 2: fecha · descripción

### Validación
- [ ] Lista ordena por `createdAt DESC` (el backend ya lo ordena)
- [ ] Se actualiza tras agregar un nuevo depósito

---

## FASE 4 — Resumen Financiero Expandido

**Archivo:** `src/pages/account/balance.tsx` (modificar)

### Tasks
- [ ] Reemplazar query `["account-balance"]` → `GET /api/account/summary` (retorna más datos)
  - Mantener compatibilidad: el campo `balance` sigue siendo el principal
- [ ] Expandir el card de saldo de 1 stat a 4:
  ```
  [ Saldo actual ]  [ Total depositado ]  [ Total entradas ]  [ Total salidas ]
  ```
  - Saldo actual: azul `#1677ff`
  - Total depositado: verde `#52c41a`
  - Total entradas: verde `#52c41a`
  - Total salidas: rojo `#ff4d4f`
- [ ] Agregar botón `<DepositModal />` en el header del card o como acción flotante

### Mobile
- [ ] Stats en grid 2x2 (xs=12 cada uno)

### Validación
- [ ] Los 4 valores se muestran correctamente formateados
- [ ] Saldo se actualiza tras cualquier depósito o movimiento

---

## FASE 5 — Integración en la página de cuenta

**Archivo:** `src/pages/account/balance.tsx` (modificar)

### Tasks
- [ ] Layout final de la página `/account`:
  ```
  ┌─────────────────────────────────────────┐
  │  Resumen Financiero          [+ Agregar] │
  │  Saldo | Depositado | Entradas | Salidas │
  └─────────────────────────────────────────┘
  ┌─────────────────────────────────────────┐
  │  Historial de depósitos                 │
  │  Tabla con fecha, monto, descripción    │
  └─────────────────────────────────────────┘
  ```
- [ ] Exportar nuevos componentes desde `src/pages/account/index.ts`
- [ ] Invalidar `["account-summary"]` también al registrar un movimiento en `MovementCreateModal`
  - Agregar `queryClient.invalidateQueries({ queryKey: ["account-summary"] })` en `onSuccess`

### Validación
- [ ] Flujo completo: agregar depósito → saldo sube → aparece en historial
- [ ] Registrar salida (movimiento) → saldo baja → summary se actualiza

---

## Archivos a crear/modificar

| Archivo | Acción |
|---------|--------|
| `src/types/account.ts` | Crear (o actualizar si existe) |
| `src/pages/account/DepositModal.tsx` | Crear |
| `src/pages/account/DepositHistory.tsx` | Crear |
| `src/pages/account/balance.tsx` | Modificar |
| `src/pages/account/index.ts` | Modificar (exportar nuevos componentes) |
| `src/pages/movements/MovementCreateModal.tsx` | Modificar (invalidar account-summary) |

---

## Orden de implementación

```
Fase 1 (Tipos) → Fase 2 (DepositModal) → Fase 3 (DepositHistory) → Fase 4 (Summary) → Fase 5 (Integración)
```

> Prerequisito: Backend `POST /api/account/deposit`, `GET /api/account/deposits` y `GET /api/account/summary` funcionando.
