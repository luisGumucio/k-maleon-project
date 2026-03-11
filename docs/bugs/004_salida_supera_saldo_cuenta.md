# BUG-004 — Salida puede superar el saldo disponible de la cuenta

## Estado
`[ ]` pendiente

## Severidad
**Alta** — el balance de la cuenta puede quedar negativo sin ningún aviso

## Descripción

El sistema permite registrar una salida cuyo monto supera el saldo disponible en `accounts.balance`. No existe ninguna validación que lo impida. El balance queda negativo — dato financiero inválido que representa dinero que no existe.

## Relación con BUG-001

Son dos validaciones independientes que deben aplicarse juntas al registrar una salida:

| Validación | Compara contra | Error si... |
|---|---|---|
| BUG-001 | `operation.total_amount` | `paid_amount + nueva_salida > total_amount` |
| BUG-004 | `account.balance` | `balance < nueva_salida` |

Ambas deben ejecutarse antes de persistir. Si cualquiera falla → rollback completo.

## Reproducción

1. Cuenta con `balance = $5,000` (500000 centavos)
2. Crear operación con `total_amount = $10,000`
3. Registrar salida de `$8,000`
4. Resultado actual: `balance = -300000` (−$3,000) — inválido
5. Resultado esperado: error `400` con mensaje claro

## Causa raíz

`AccountService.java` línea 39 — resta sin validar:

```java
void debit(Account account, Long amount) {
    account.setBalance(account.getBalance() - amount);  // sin guard
    accountRepository.save(account);
}
```

No existe ninguna comparación entre `account.getBalance()` y `amount` antes de ejecutar el débito.

## Fix requerido

### 1. Validación en `MovementService.java`

Agregar junto a la validación del BUG-001, antes de persistir, cuando `type == "salida"`:

```java
if (request.getAmount() > account.getBalance()) {
    throw new IllegalArgumentException(
        "Saldo insuficiente. Disponible: " + account.getBalance()
        + ", requerido: " + request.getAmount()
    );
}
```

El orden recomendado de validaciones:

```java
// 1. Validar que no supera el total acordado de la operación (BUG-001)
long newPaidAmount = operation.getPaidAmount() + request.getAmount();
if (newPaidAmount > operation.getTotalAmount()) {
    throw new IllegalArgumentException(
        "El monto supera el total acordado de la operación"
    );
}

// 2. Validar saldo suficiente en cuenta (BUG-004)
if (request.getAmount() > account.getBalance()) {
    throw new IllegalArgumentException(
        "Saldo insuficiente. Disponible: " + account.getBalance()
        + ", requerido: " + request.getAmount()
    );
}
```

### 2. Tests a agregar en `MovementServiceTest.java`

**Error path — saldo insuficiente:**
```
whenSalida_exceedsAccountBalance_thenThrowsAndNothingIsPersisted
- account.balance = 500000 ($5,000)
- request.amount  = 800000 ($8,000)
- expected: IllegalArgumentException "Saldo insuficiente"
- verify: movementRepository never saved, accountService never debited,
          operation.paidAmount unchanged
```

**Edge case — saldo exactamente igual al monto:**
```
whenSalida_exactlyEqualsBalance_thenSucceeds
- account.balance = 500000
- request.amount  = 500000
- expected: balance queda en 0, movimiento registrado correctamente
```

**Edge case — ambas validaciones fallan (BUG-001 + BUG-004):**
```
whenSalida_exceedsBothTotalAndBalance_thenThrowsBUG001First
- La validación de total_amount se ejecuta primero
- Si pasa esa, se valida el balance
- Solo se lanza una excepción (la primera que falla)
```

## Archivos a modificar

- `src/main/java/com/kmaleon/service/MovementService.java` — agregar ambas validaciones juntas (BUG-001 + BUG-004)
- `src/test/java/com/kmaleon/service/MovementServiceTest.java` — nuevos casos de error

## Nota sobre el frontend

Cuando el backend retorne `400` con el mensaje de error, el frontend ya lo muestra correctamente en `handleFinish` mediante `message.error(...)`. No requiere cambios en el frontend.
