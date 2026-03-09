# BUG-001 — Salida puede superar el monto acordado de la operación

## Estado
`[ ]` pendiente

## Severidad
**Alta** — corrupción silenciosa de datos financieros

## Descripción

El sistema permite registrar una salida cuyo monto supera el `total_amount` de la operación. No hay ninguna validación que lo impida. El `paid_amount` queda mayor que `total_amount`, y `pending_amount` resulta negativo — dato financiero inválido.

## Reproducción

1. Crear una operación con `total_amount = $1,000` (100000 centavos)
2. Registrar una salida de `$1,500` (150000 centavos)
3. Resultado: `paid_amount = 150000`, `pending_amount = -50000`

## Causa raíz

`MovementService.java` línea 57 — acumula sin validar:

```java
operation.setPaidAmount(operation.getPaidAmount() + request.getAmount());
```

No existe ningún guard antes de esta línea que compare contra `totalAmount`.

## Cobertura de tests actual

`MovementServiceTest.java` no tiene ningún caso que cubra este escenario.
El test `whenMultipleSalidas_thenPaidAmountAccumulates` solo verifica acumulación
normal (300k + 200k = 500k sobre un total de 1000k) — nunca supera el límite.

## Fix requerido

### 1. Validación en `MovementService.java`

Agregar antes de persistir, cuando `type == "salida"`:

```java
long newPaidAmount = operation.getPaidAmount() + request.getAmount();
if (newPaidAmount > operation.getTotalAmount()) {
    throw new IllegalArgumentException(
        "El monto de la salida supera el monto acordado de la operación"
    );
}
```

### 2. Tests a agregar en `MovementServiceTest.java`

**Error path:**
```
whenSalida_exceedsTotalAmount_thenThrowsAndNothingIsPersisted
- operation.totalAmount = 1000000, operation.paidAmount = 0
- request.amount = 1500000
- expected: IllegalArgumentException
- verify: movementRepository never saved, accountService never debited
```

**Edge case — pago exacto al límite:**
```
whenSalida_exactlyEqualsTotalAmount_thenSucceeds
- operation.totalAmount = 1000000, operation.paidAmount = 0
- request.amount = 1000000
- expected: paidAmount == totalAmount, pendingAmount == 0
```

**Edge case — pago parcial previo + nueva salida supera límite:**
```
whenSalida_withExistingPaidAmount_exceedsTotal_thenThrows
- operation.totalAmount = 1000000, operation.paidAmount = 800000
- request.amount = 300000  (800k + 300k = 1100k > 1000k)
- expected: IllegalArgumentException
```

## Archivos a modificar

- `src/main/java/com/kmaleon/service/MovementService.java`
- `src/test/java/com/kmaleon/service/MovementServiceTest.java`
