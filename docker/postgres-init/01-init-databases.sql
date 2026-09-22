-- Inicializacion de PostgreSQL para Household Finance Platform.
-- Este script se ejecuta automaticamente una unica vez, la primera vez que se crea
-- el volumen de datos de postgres, gracias al mecanismo de docker-entrypoint-initdb.d.
--
-- Crea una base de datos y un usuario propio por cada microservicio, de forma que
-- cada servicio sea duenio exclusivo de sus datos (sin consultas cruzadas) aunque
-- todos compartan la misma instancia de PostgreSQL en este entorno de desarrollo.
--
-- IMPORTANTE: los valores deben coincidir con IDENTITY_DB_*/FINANCE_DB_* en tu .env.
-- Si cambias esas variables, actualiza tambien este script antes del primer arranque
-- (una vez creado el volumen de datos, este script ya no se vuelve a ejecutar).

-- --- identity-service ---
CREATE USER identity_user WITH PASSWORD 'changeme-identity';
CREATE DATABASE identity_db OWNER identity_user;
REVOKE ALL PRIVILEGES ON DATABASE identity_db FROM PUBLIC;
GRANT ALL PRIVILEGES ON DATABASE identity_db TO identity_user;

-- --- household-finance-service ---
CREATE USER finance_user WITH PASSWORD 'changeme-finance';
CREATE DATABASE finance_db OWNER finance_user;
REVOKE ALL PRIVILEGES ON DATABASE finance_db FROM PUBLIC;
GRANT ALL PRIVILEGES ON DATABASE finance_db TO finance_user;
