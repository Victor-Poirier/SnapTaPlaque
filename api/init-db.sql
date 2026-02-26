-- ==============================================================================
-- init-db.sql — Database Initialization Script for SnapTaPlaque
-- ==============================================================================
-- This SQL script is executed automatically by PostgreSQL on the very first
-- container startup, when the database volume is empty and the database is
-- being created for the first time.
--
-- It is typically mounted into the PostgreSQL container via a Docker volume
-- mapping to the /docker-entrypoint-initdb.d/ directory:
--
--   volumes:
--     - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
--
-- PostgreSQL runs all .sql (and .sh) files found in that directory in
-- alphabetical order during the initial `initdb` phase only. On subsequent
-- restarts (when the data volume already exists), this script is NOT re-run.
--
-- What this script does:
--   1. Enables the uuid-ossp extension (provides UUID generation functions
--      such as uuid_generate_v4(), useful if UUIDs are ever needed as PKs).
--   2. Grants full privileges on the snaptaplaque_db database to the
--      plate_user role, ensuring the application has the necessary
--      permissions to create tables, insert data, run migrations, etc.
--   3. Outputs a confirmation message to the PostgreSQL startup logs.
--
-- Prerequisites:
--   - The database "snaptaplaque_db" and role "plate_user" must already exist.
--     They are created automatically by the POSTGRES_DB / POSTGRES_USER
--     environment variables defined in docker-compose.yml.
--
-- NOTE: Schema creation (tables, indexes, constraints) is handled by Alembic
--       migrations, NOT by this script. This file only performs one-time
--       database-level setup.
-- ==============================================================================

-- Enable the uuid-ossp extension for UUID generation functions.
-- IF NOT EXISTS ensures idempotency (no error if already enabled).
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant all privileges on the snaptaplaque_db database to plate_user.
-- This includes CREATE, CONNECT, TEMPORARY, and allows the user to
-- create and manage schemas, tables, and other database objects.
GRANT ALL PRIVILEGES ON DATABASE snaptaplaque_db TO plate_user;

-- Output a confirmation message to the PostgreSQL server logs so operators
-- can verify that the initialization script ran successfully.
SELECT 'Base de données initialisée avec succès' AS status;
