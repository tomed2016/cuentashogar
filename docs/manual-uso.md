# Manual de uso y pruebas

Este manual describe cómo ejecutar el MVP de Household Finance Platform y en
qué orden probar sus flujos. El alcance actual contiene dos servicios
ejecutables:

- `identity-service`: registro, autenticación, refresh, logout y usuario actual.
- `household-finance-service`: hogares, cuentas, categorías, movimientos,
  transferencias, facturas, vencimientos, presupuestos y resumen mensual.

Los BFF web y mobile todavía no están implementados. Para probar el MVP se
utilizan directamente los dos servicios REST.

## 1. Requisitos

- Windows con PowerShell.
- Docker Desktop iniciado.
- Git.
- Java 21 para ejecutar Maven localmente.
- Postman Desktop si se utilizará la colección incluida.

Las versiones configuradas siguen siendo Java 21 y Spring Boot 3.3.5. No es
necesario instalar Java 25 para esta versión del proyecto.

## 2. Preparar y levantar el entorno

Desde `household-finance-platform`:

```powershell
Copy-Item .env.example .env
docker compose up -d --build
docker compose ps
```

Esperar hasta que `identity-service` y `household-finance-service` aparezcan
como `healthy`. Se puede comprobar cada servicio con:

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
```

URLs locales:

| Servicio | URL base | Swagger UI |
|---|---|---|
| Identity | `http://localhost:8081` | `http://localhost:8081/swagger-ui.html` |
| Finance | `http://localhost:8082` | `http://localhost:8082/swagger-ui.html` |
| PostgreSQL | `localhost:5432` | No aplica |

Si se modifican variables de base de datos después de haber creado el volumen,
el script de inicialización no se ejecuta nuevamente. Para comenzar con bases
vacías:

```powershell
docker compose down -v
docker compose up -d --build
```

## 3. Flujo recomendado: qué va primero

Ejecutar los pasos siguientes en este orden:

1. **Registrar un usuario** en Identity. La respuesta entrega un access token y
   un refresh token.
2. **Iniciar sesión** para obtener un token limpio si el registro ya se había
   ejecutado.
3. **Consultar el usuario actual** y confirmar que el token funciona.
4. **Crear un hogar**. El usuario autenticado queda como administrador.
5. **Crear una categoría** para asociarla a ingresos o gastos.
6. **Crear dos cuentas** en la misma moneda, por ejemplo USD. La segunda cuenta
   permite probar transferencias.
7. **Registrar un ingreso** en la primera cuenta y comprobar el saldo.
8. **Registrar un gasto** y comprobar nuevamente el saldo.
9. **Transferir dinero** entre las dos cuentas.
10. **Crear una factura**, generar su vencimiento y pagarlo desde la cuenta
    principal. El servicio crea automáticamente el movimiento `BILL_PAYMENT` y
    descuenta el importe de la cuenta.
11. **Crear un presupuesto** asociado a una categoría.
12. **Consultar el resumen mensual**, cuentas, movimientos y facturas.

La colección de Postman automatiza este flujo y guarda los UUID devueltos por
la API en variables de entorno.

## 4. Probar con Postman

### Importar los archivos

En Postman:

1. Seleccionar **Import**.
2. Importar `postman/household-finance-platform.postman_collection.json`.
3. Importar `postman/household-finance-platform.postman_environment.json`.
4. Seleccionar el entorno **Household Finance - Local** en la esquina superior
   derecha.
5. Abrir la colección y ejecutar la carpeta **01 - Identity**.
6. Ejecutar después la carpeta **02 - Household and finance** completa o sus
   peticiones en orden.

La petición **Register user** genera un correo único con UUID. La contraseña de
prueba es `Password123!`. Los scripts guardan automáticamente:
`access_token`, `refresh_token_id`, `refresh_token`, `household_id`,
`account_id`, `account_2_id`, `category_id`, `bill_id`,
`occurrence_id` y `budget_id`.

Las peticiones financieras usan automáticamente:

```text
Authorization: Bearer {{access_token}}
```

No se deben copiar manualmente los UUID entre peticiones salvo que se esté
probando un caso aislado. Para pagar un vencimiento se utiliza `account_id`;
no es necesario enviar un `transaction_id` generado previamente.

### Ejecutar la colección completa

La forma más simple es abrir el **Collection Runner**, seleccionar el entorno y
ejecutar la colección. Debe conservarse el orden de las carpetas y no deben
ejecutarse peticiones financieras antes de crear el hogar y las cuentas.

También se puede usar Newman:

```powershell
npx newman run .\postman\household-finance-platform.postman_collection.json `
  -e .\postman\household-finance-platform.postman_environment.json
```

Si el correo se conserva entre ejecuciones, el registro puede devolver
conflicto. La colección genera un correo nuevo para cada ejecución; si se
ejecuta una petición individual de login, usar el valor actual de `email` del
entorno.

## 5. Casos de prueba manuales

### Autenticación

- Registrar un usuario válido: debe responder `201`.
- Iniciar sesión con contraseña incorrecta: debe rechazarse.
- Consultar `/api/v1/users/me` con token: debe responder `200`.
- Renovar el token con `refresh_token_id` y `refresh_token`.
- Cerrar sesión y comprobar que el refresh token ya no puede renovarse.

### Autorización

- Quitar el header `Authorization` de una petición financiera: debe responder
  `401`.
- Usar un UUID de hogar inexistente o perteneciente a otro usuario: no debe
  permitir consultar ni modificar los datos.

### Finanzas

- Crear una cuenta con saldo inicial.
- Registrar un ingreso y verificar que el saldo aumenta.
- Registrar un gasto y verificar que el saldo disminuye.
- Transferir entre dos cuentas y verificar ambos saldos.
- Intentar transferir a la misma cuenta o por un importe inválido.
- Crear un vencimiento duplicado para la misma factura y fecha: la restricción
  de persistencia debe impedirlo.
- Crear un presupuesto y verificar que se conserva después de reiniciar el
  servicio.

## 6. Comandos de pruebas automatizadas

Desde `household-finance-platform`:

```powershell
.\mvnw.cmd test
```

Esto ejecuta las pruebas unitarias y las pruebas de arquitectura. Para la
verificación completa, incluyendo Failsafe y Testcontainers:

```powershell
.\mvnw.cmd clean verify
```

Docker Desktop debe estar iniciado para las pruebas de integración. En el
entorno de desarrollo actual Testcontainers presentó un problema de handshake
con Docker Engine aunque la CLI de Docker funcionaba; si vuelve a ocurrir,
revisar el contexto activo de Docker Desktop y ejecutar la verificación en una
máquina con acceso directo al API del daemon.

## 7. Observaciones importantes del MVP

- El endpoint de pago de vencimientos recibe `accountId`, crea un movimiento
  `BILL_PAYMENT`, descuenta la cuenta y marca el vencimiento como pagado en
  una única transacción. Un pago repetido es rechazado.
- No hay todavía idempotencia persistente mediante `Idempotency-Key`.
- Los BFF web y mobile todavía no forman parte del proyecto ejecutable.
- Las consultas avanzadas de facturas próximas/vencidas y el consumo completo
  de presupuestos aún están pendientes.
- Las operaciones financieras requieren que los importes y monedas sean
  compatibles; usar siempre la misma moneda en una prueba sencilla.

## 8. Detener el entorno

Detener servicios conservando datos:

```powershell
docker compose down
```

Detener servicios y eliminar las bases de datos locales:

```powershell
docker compose down -v
```

Si Flyway informa un `checksum mismatch` después de actualizar una migración
inicial en un entorno local existente, utilizar este procedimiento para
recrear el volumen de desarrollo. En una base de datos con información que
deba conservarse no se debe borrar el volumen: hay que ejecutar una migración
nueva o un procedimiento controlado de `flyway repair`.
