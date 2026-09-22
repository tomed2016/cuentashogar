# Manual de pruebas con Postman

Este documento explica cómo probar `identity-service` y
`household-finance-service` utilizando la colección incluida en el proyecto.

## Archivos de Postman

| Archivo | Descripción |
|---|---|
| `postman/household-finance-platform.postman_collection.json` | Colección Postman 2.1 |
| `postman/household-finance-platform.postman_environment.json` | Variables del entorno local |

## Requisitos

- Docker Desktop iniciado.
- Servicios levantados en los puertos `8081` y `8082`.
- Postman Desktop o Newman.

Desde `household-finance-platform`:

```powershell
Copy-Item .env.example .env
docker compose up -d --build
docker compose ps
```

Validar la salud de los servicios:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
```

## Importar la colección

1. Abrir Postman.
2. Seleccionar **Import**.
3. Importar `postman/household-finance-platform.postman_collection.json`.
4. Importar `postman/household-finance-platform.postman_environment.json`.
5. Seleccionar **Household Finance - Local** como entorno activo.

La colección contiene dos carpetas:

- `01 - Identity`
- `02 - Household and finance`

## Ejecutar el flujo completo

Abrir **Collection Runner**, seleccionar la colección y el entorno
**Household Finance - Local**. Ejecutar todas las peticiones respetando su
orden.

La colección genera un usuario nuevo en cada ejecución. Las credenciales son:

```text
Correo: generado automáticamente como mvp-<uuid>@example.com
Contraseña: Password123!
```

Los scripts de Postman guardan automáticamente los valores devueltos por la
API. Entre ellos:

```text
access_token
refresh_token_id
refresh_token
household_id
account_id
account_2_id
category_id
bill_id
occurrence_id
budget_id
```

Las peticiones protegidas utilizan:

```http
Authorization: Bearer {{access_token}}
```

## Ejecución por carpeta

### Carpeta `01 - Identity`

Ejecutar en este orden:

1. `Register user`
2. `Login`
3. `Get current user`
4. `Refresh access token`

El registro y el login guardan los tokens. El refresh rota el refresh token y
actualiza las variables del entorno.

### Carpeta `02 - Household and finance`

Ejecutar en este orden:

1. `Create household`
2. `Get household`
3. `Create category`
4. `List categories`
5. `Create primary account`
6. `Create savings account`
7. `Register income`
8. `Register expense`
9. `Transfer between accounts`
10. `List accounts`
11. `List transactions`
12. `Create bill`
13. `List bills`
14. `Generate bill occurrence`
15. `Pay bill occurrence` (usa `account_id` y crea el movimiento automáticamente)
16. `Create budget`
17. `Monthly summary`

No se deben ejecutar peticiones de esta carpeta antes de crear el hogar y
obtener `household_id`.

## Ejecutar con Newman

Instalar Newman si todavía no está disponible:

```powershell
npm install -g newman
```

Ejecutar:

```powershell
npx newman run .\postman\household-finance-platform.postman_collection.json `
  -e .\postman\household-finance-platform.postman_environment.json
```

## Probar una petición individual

Para probar una petición aislada, primero deben existir las variables que
requiere su URL o cuerpo. Por ejemplo, `List accounts` requiere:

```text
finance_url
access_token
household_id
```

Los UUID se pueden consultar en las respuestas anteriores o en el entorno
activo de Postman.

El pago de un vencimiento no requiere un movimiento ni un `transaction_id`
preexistente. La petición utiliza `account_id`; el servicio toma el importe de la factura,
crea el movimiento `BILL_PAYMENT`, actualiza el saldo y marca el vencimiento
como pagado en una única transacción.

## Reiniciar las pruebas

Para detener los servicios sin eliminar datos:

```powershell
docker compose down
```

Para comenzar con una base de datos limpia:

```powershell
docker compose down -v
docker compose up -d --build
```

El segundo comando elimina los volúmenes locales de PostgreSQL.
