# Frontend Plan — Feature: SWIFT PDF Parser

> Validar cada fase antes de avanzar a la siguiente.
> Estado: `[ ]` pendiente | `[x]` completado | `[~]` en progreso

---

## Contexto

Cuando el usuario crea un movimiento de tipo SWIFT, puede subir el comprobante PDF. Una vez subido, aparece un botón **"Extraer datos del PDF"** que llama al backend (`POST /api/attachments/parse-swift`) y rellena automáticamente los campos de metadata SWIFT en el formulario. Los campos quedan editables para que el usuario pueda corregir.

---

## Flujo completo

```
1. Usuario selecciona tipo "SWIFT"
2. Sube el PDF → handleUpload() → uploadedUrl queda seteado
3. Si uploadedUrl existe y el archivo es .pdf → mostrar botón "Extraer datos del PDF"
4. Usuario clickea el botón → llamar parse-swift con la url
5. Backend retorna campos parseados
6. form.setFieldsValue({ metadata: { ... } }) rellena el formulario
7. Campos quedan editables — usuario puede corregir antes de guardar
```

**Notas:**
- Si el usuario sube un nuevo PDF, sobreescribe el anterior (mismo comportamiento actual del upload)
- La extracción es manual (botón explícito), no automática al subir
- Si el backend retorna null en algún campo, ese campo queda vacío (no sobreescribir con null)

---

## FASE 1 — Función de llamada al parser

**Objetivo:** Encapsular la llamada a `POST /api/attachments/parse-swift`.

### Tasks
- [x] En `MovementCreateModal.tsx`, agregar estado:
  ```ts
  const [parsing, setParsing] = useState(false);
  ```
- [x] Agregar función `handleParseSwift`:
  - Llama a `fetchWithAuth(${apiUrl}/attachments/parse-swift, { method: "POST", body: JSON.stringify({ url: uploadedUrl }) })`
  - Mapea la respuesta a los nombres de campo del formulario (camelCase → snake_case):
    ```ts
    {
      message_id: data.messageId,
      uetr: data.uetr,
      settlement_date: data.settlementDate ? dayjs(data.settlementDate) : undefined,
      debtor_bank: data.debtorBank,
      debtor_bic: data.debtorBic,
      debtor_account: data.debtorAccount,
      creditor_bank: data.creditorBank,
      creditor_bic: data.creditorBic,
      creditor_name: data.creditorName,
      creditor_account: data.creditorAccount,
      remittance: data.remittance,
      charge_bearer: data.chargeBearer,
    }
    ```
  - Filtrar claves con valor `undefined` o `null` antes de setear
  - Llamar `form.setFieldsValue({ metadata: filteredFields })`
  - `message.success("Datos extraídos correctamente")`
  - En catch: `message.error("No se pudo extraer datos del PDF")`
  - Siempre: `setParsing(false)`

### Validación
- [x] La función compila sin errores de TypeScript

---

## FASE 2 — Botón en la UI

**Objetivo:** Mostrar el botón de extracción cuando corresponde.

### Tasks
- [x] En `MovementCreateModal.tsx`, dentro del `Form.Item` del comprobante, cuando `uploadedUrl` existe:
  - Si `selectedPaymentType === "swift"` y `uploadedFileName` termina en `.pdf`:
    - Mostrar botón adicional bajo el link del archivo:
      ```tsx
      <Button
        size="small"
        icon={<FileSearchOutlined />}
        loading={parsing}
        onClick={handleParseSwift}
      >
        Extraer datos del PDF
      </Button>
      ```
  - El botón aparece junto al link y el botón de eliminar (mismo row)
- [x] Importar `FileSearchOutlined` desde `@ant-design/icons`

### Validación
- [x] El botón aparece solo cuando: tipo es SWIFT + archivo subido es PDF
- [x] El botón NO aparece para otros tipos de pago
- [x] El botón NO aparece si el archivo subido es imagen (jpg/png)

---

## FASE 3 — Resetear estado al cambiar tipo de pago

**Objetivo:** Evitar que el botón de extracción quede visible si el usuario cambia de SWIFT a otro tipo.

### Tasks
- [x] En el `onChange` del Select de "Método de pago" (`handlePaymentTypeChange`):
  - No tocar el archivo subido (el comprobante sigue siendo válido)
  - Solo la visibilidad del botón cambia porque depende de `selectedPaymentType`
  - (No requiere cambio de código — ya funciona por la condición en Fase 2)

### Validación
- [x] Cambiar de SWIFT a Transferencia oculta el botón de extracción
- [x] El comprobante subido permanece aunque se cambie el tipo

---

## FASE 4 — Reset al cerrar/abrir modal

**Objetivo:** Limpiar el estado `parsing` al resetear el modal.

### Tasks
- [x] En `handleOpen()`, agregar: `setParsing(false)`
- [x] Verificar que `destroyOnClose` en el Modal ya resetea el form (ya está configurado)

### Validación
- [x] Abrir el modal → subir PDF → extraer → cerrar → volver a abrir: todo limpio

---

## Orden de implementación

```
Fase 1 → Fase 2 → Fase 3 → Fase 4
```
