# Flujos de prueba con Postman

Este documento define qué se debe comprobar en cada flujo del MVP y cuál es
el resultado esperado.

## Flujo 1: registro y autenticación

### Objetivo

Verificar que un usuario pueda registrarse, iniciar sesión y obtener tokens.

### Peticiones

1. `POST /api/v1/auth/register`
2. `POST /api/v1/auth/login`
3. `GET /api/v1/users/me`
4. `POST /api/v1/auth/refresh`

### Resultado esperado

- Registro: `201 Created`.
- Login: `200 OK`.
- Usuario actual: `200 OK`.
- Refresh: `200 OK`.
- La respuesta contiene `accessToken`, `refreshTokenId` y `refreshToken`.

## Flujo 2: creación del hogar

### Objetivo

Crear un hogar con el usuario autenticado.

### Peticiones

1. `POST /api/v1/households`
2. `GET /api/v1/households/{householdId}`

### Resultado esperado

- Creación: `201 Created`.
- El usuario creador queda registrado como administrador del hogar.
- El identificador se guarda en `household_id`.

## Flujo 3: cuentas y saldos

### Objetivo

Validar la creación de cuentas y el movimiento de saldos.

### Peticiones

1. Crear cuenta principal con saldo `0 USD`.
2. Crear cuenta de ahorro con saldo `500 USD`.
3. Registrar ingreso de `2500 USD` en la cuenta principal.
4. Registrar gasto de `100 USD`.
5. Listar las cuentas.

### Resultado esperado

El saldo de la cuenta principal debe reflejar:

```text
0 + 2500 - 100 = 2400 USD
```

La cuenta de ahorro debe conservar inicialmente `500 USD`.

## Flujo 4: transferencia

### Objetivo

Verificar una transferencia entre dos cuentas del mismo hogar.

### Petición

```http
POST /api/v1/households/{householdId}/transfers
```

Con un importe de `200 USD` desde `account_id` hacia `account_2_id`.

### Resultado esperado

```text
Cuenta principal: 2400 - 200 = 2200 USD
Cuenta de ahorro: 500 + 200 = 700 USD
```

No debe utilizarse la misma cuenta como origen y destino.

## Flujo 5: factura y vencimiento

### Objetivo

Crear una factura, generar un vencimiento y pagarlo desde una cuenta del mismo
hogar mediante una operación financiera atómica.

### Peticiones

1. `POST /api/v1/households/{householdId}/bills`
2. `GET /api/v1/households/{householdId}/bills`
3. `POST /api/v1/households/{householdId}/bills/{billId}/occurrences`
4. `POST /api/v1/households/{householdId}/bill-occurrences/{occurrenceId}/payments`

El pago requiere el header `Idempotency-Key`. La petición de generación del
vencimiento de la colección crea automáticamente una clave nueva y la guarda
en `payment_idempotency_key`.

### Cuerpo del pago

```json
{
  "accountId": "{{account_id}}",
  "description": "Pago de electricidad"
}
```

### Resultado esperado

- La factura se crea con `201 Created`.
- El vencimiento se crea con estado pendiente.
- El pago responde `200 OK` y cambia el vencimiento a `PAID`.
- Se crea un movimiento de tipo `BILL_PAYMENT`.
- El saldo de `account_id` disminuye exactamente por el importe de la factura.
- `paidBy` contiene el identificador del movimiento creado.
- Repetir la misma petición con la misma clave devuelve el mismo vencimiento y
  no crea otro movimiento ni vuelve a descontar el saldo.
- Reutilizar la clave con otro vencimiento, cuenta o descripción es rechazado.

Los endpoints de ingresos, gastos y transferencias también requieren
`Idempotency-Key`. La colección genera claves independientes con los prefijos
`income-`, `expense-` y `transfer-`.
- Un segundo intento sobre el mismo vencimiento falla y no descuenta nuevamente
  el saldo.
- Una cuenta inexistente, de otro hogar, con otra moneda o sin saldo suficiente
  también debe fallar sin persistir cambios parciales.

En la implementación actual, las reglas de dominio se exponen como `422
Unprocessable Entity`; el cliente debe tratar ese resultado como un rechazo
del pago y conservar el saldo anterior.

## Flujo 6: presupuesto y resumen

### Objetivo

Crear un presupuesto y consultar el resumen del período.

### Peticiones

1. `POST /api/v1/households/{householdId}/budgets`
2. `GET /api/v1/households/{householdId}/reports/summary?from=2026-01-01&to=2026-12-31`

### Resultado esperado

- El presupuesto se crea con `201 Created`.
- La categoría y la moneda del límite son válidas.
- El resumen responde correctamente para el período enviado.

## Flujo 7: seguridad y autorización

### Caso sin token

Eliminar el header `Authorization` de una petición financiera.

Resultado esperado:

```text
401 Unauthorized
```

### Caso de token inválido

Usar un token modificado o expirado.

Resultado esperado:

```text
401 Unauthorized
```

### Caso de datos inválidos

Enviar un importe negativo, una moneda vacía o un nombre vacío.

Resultado esperado:

```text
400 Bad Request
```

## Flujo 8: persistencia

### Objetivo

Verificar que los datos sobreviven al reinicio de los servicios.

### Procedimiento

1. Ejecutar la colección hasta crear hogar, cuentas, movimientos, factura y
   presupuesto.
2. Ejecutar:

   ```powershell
   docker compose restart identity-service household-finance-service
   ```

3. Esperar a que ambos servicios vuelvan a estar saludables.
4. Repetir `Get household`, `List accounts`, `List transactions` y `List bills`.

### Resultado esperado

Los datos deben seguir disponibles porque se almacenan en PostgreSQL. No usar
`docker compose down -v` para esta prueba, porque ese comando elimina los
volúmenes.

## Matriz de resultados

| Flujo | Petición principal | Resultado esperado |
|---|---|---|
| Registro | `POST /auth/register` | `201` |
| Login | `POST /auth/login` | `200` |
| Usuario actual | `GET /users/me` | `200` |
| Hogar | `POST /households` | `201` |
| Cuenta | `POST /accounts` | `201` |
| Ingreso/gasto | `POST /transactions` | `201` |
| Transferencia | `POST /transfers` | `201` |
| Factura | `POST /bills` | `201` |
| Vencimiento | `POST /occurrences` | `201` |
| Pago | `POST /payments` | `200` |
| Presupuesto | `POST /budgets` | `201` |
| Resumen | `GET /reports/summary` | `200` |
