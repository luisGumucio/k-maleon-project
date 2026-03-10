# BUG-003 — Error de compilación en unit tests del backend

## Estado: RESUELTO

## Síntoma

Los tests del backend no compilan. Errores en `AccountServiceTest` y `MovementServiceTest`.

## Causa

Al implementar la autenticación y el aislamiento de datos por `ownerId` (migración 015),
`AccountService` cambió sus firmas de métodos. Los tests quedaron con las firmas antiguas:

| Test | Firma usada en el test | Firma real actual |
|---|---|---|
| `AccountServiceTest.whenGetBalance_*` | `accountService.getBalance()` | `accountService.getBalance(UUID callerId)` |
| `AccountServiceTest.whenSetInitialBalance_*` | `accountService.setInitialBalance(Long)` | `accountService.setInitialBalance(UUID, Long)` |
| `AccountServiceTest.whenGetAccount_*` | `accountService.getAccount()` | `accountService.getAccount(UUID callerId)` |
| `AccountServiceTest` (mocks) | `accountRepository.findAll()` | `accountRepository.findByOwnerId(UUID)` |
| `MovementServiceTest` | `accountService.getAccount()` | `accountService.getAccount(UUID callerId)` |

Además, `credit()` y `debit()` son package-private en `AccountService` — los tests de
`MovementServiceTest` los mockean con `@Mock` lo cual funciona con Mockito, pero si se
cambia la visibilidad hay que verificar.

## Solución

Actualizar los tests para usar las nuevas firmas:

### `AccountServiceTest`

1. Usar un `UUID callerId` fijo como constante en los tests.
2. Reemplazar `accountService.getBalance()` → `accountService.getBalance(callerId)`.
3. Reemplazar `accountService.setInitialBalance(amount)` → `accountService.setInitialBalance(callerId, amount)`.
4. Reemplazar `accountService.getAccount()` → `accountService.getAccount(callerId)`.
5. Reemplazar mock de `accountRepository.findAll()` → `accountRepository.findByOwnerId(callerId)`.

### `MovementServiceTest`

1. El mock de `accountService.getAccount()` debe ser `accountService.getAccount(any())`.
   O bien mockear con `when(accountService.getAccount(any())).thenReturn(account)`.

## Archivos afectados

- `backend/src/test/java/com/kmaleon/service/AccountServiceTest.java`
- `backend/src/test/java/com/kmaleon/service/MovementServiceTest.java`

## Archivos de referencia (código fuente actual)

- `backend/src/main/java/com/kmaleon/service/AccountService.java`
- `backend/src/main/java/com/kmaleon/repository/AccountRepository.java`
