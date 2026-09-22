# Datos de prueba del MVP

La colección `postman/household-finance-platform.postman_collection.json` es una
colección Postman 2.1 y genera datos nuevos en cada ejecución. No requiere
UUIDs predefinidos ni modificar la base de datos. El procedimiento completo
está en [`manual-uso.md`](manual-uso.md).

## Preparación

1. Ejecutar `docker compose up -d` desde la raíz.
2. Importar la colección y `postman/household-finance-platform.postman_environment.json` en Postman.
3. Seleccionar el entorno **Household Finance - Local**.
4. Ejecutar las carpetas en orden: `01 - Identity` y luego `02 - Household and finance`.

Los scripts de Postman guardan automáticamente `access_token`, `refresh_token`, `household_id`, `account_id`, `category_id`, `transaction_id`, `bill_id` y `occurrence_id`.

## Datos generados

| Recurso | Datos |
|---|---|
| Usuario | Correo UUID aleatorio, `Ana Gomez`, contraseña `Password123!` |
| Hogar | `Hogar MVP` |
| Categoría | `Salario` |
| Cuenta 1 | `Cuenta principal`, tipo `BANK`, USD 0 |
| Cuenta 2 | `Cuenta ahorro`, tipo `BANK`, USD 500 |
| Ingreso | USD 2.500, tipo `INCOME` |
| Gasto | USD 100, tipo `EXPENSE` |
| Transferencia | USD 200 entre las dos cuentas |
| Tarjeta | `Visa principal`, límite USD 3.000 |
| Factura | `Electricidad`, USD 80, vencimiento `2026-10-10`; el pago debita `Cuenta principal` |
| Presupuesto | Categoría `Salario`, USD 500, octubre de 2026 |

## Ejecución desde Newman

```powershell
npx newman run .\postman\household-finance-platform.postman_collection.json `
  -e .\postman\household-finance-platform.postman_environment.json
```

La colección está diseñada para ejecutarse secuencialmente. Si se ejecuta una
petición aislada, deben definirse manualmente sus variables de entorno. El
pago del vencimiento utiliza `account_id`, crea un movimiento
`BILL_PAYMENT`, descuenta USD 80 de la cuenta y guarda el identificador del
movimiento en `paidBy`.

## Reinicio de datos

Para eliminar los datos PostgreSQL locales y comenzar desde cero:

```powershell
docker compose down -v
docker compose up -d
```

El repositorio en memoria de Finance se reinicia automáticamente cuando se recrea el contenedor del servicio.
