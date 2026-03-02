#!/bin/bash
# ==============================================================================
# setup.sh — Full Database & Application Bootstrap Script for SnapTaPlaque API
# ==============================================================================
#
# This shell script performs a complete, one-time setup of the SnapTaPlaque
# local development environment. It is intended to be run on a host machine
# where PostgreSQL is installed natively (i.e., NOT inside a Docker container).
#
# Usage:
#   chmod +x setup.sh
#   ./setup.sh
#
# Prerequisites:
#   - PostgreSQL server installed and running locally
#   - The current OS user has sudo access to the "postgres" system account
#   - Python 3.10+ with pip dependencies installed (see requirements.txt)
#   - Alembic configured with a valid alembic.ini and migrations directory
#   - The project's Python packages (app.database, app.crud, app.models)
#     are importable from the current working directory
#
# What this script does (in order):
#   1. Creates the PostgreSQL role (plate_user) and database (snaptaplaque_db)
#      if they do not already exist, then grants full privileges on the
#      database, public schema, tables, and sequences.
#   2. Runs `alembic upgrade head` to apply all pending database migrations,
#      creating the application tables (users, predictions, etc.).
#   3. Seeds a default administrator account via an inline Python script,
#      skipping creation if the admin user already exists (idempotent).
#
# Configuration:
#   All credentials and identifiers are defined as variables at the top of
#   the script for easy customisation. In production, replace hard-coded
#   values with environment variables or a secrets manager.
#
# Exit behaviour:
#   The script uses `set -e` so it will abort immediately on the first
#   command that returns a non-zero exit code. This prevents partial setups
#   (e.g., running migrations against a database that failed to create).
#
# WARNING:
#   This script contains hard-coded database and admin credentials suitable
#   for local development only. NEVER use these values in a production
#   deployment. Rotate all passwords before going live.
#
# ==============================================================================

# Abort the script immediately if any command exits with a non-zero status.
# This ensures failures in database creation or migrations halt the process
# before subsequent steps run against an incomplete environment.
set -e

# ------------------------------------------------------------------------------
# Configuration Variables
# ------------------------------------------------------------------------------
# These variables control the database name, credentials, and the initial
# admin account. Override them or source them from a .env file for non-default
# environments.
# ------------------------------------------------------------------------------
DB_NAME="snaptaplaque_db"        # Name of the PostgreSQL database to create
DB_USER="plate_user"             # PostgreSQL role used by the application
DB_PASSWORD="plate_password"     # Password for DB_USER — CHANGE for production!
ADMIN_EMAIL="admin@example.com"  # Email address for the seed admin account
ADMIN_USERNAME="admin"           # Username for the seed admin account
ADMIN_PASSWORD="AdminP@ssw0rd"   # Password for the seed admin account

echo "🚀 Création de la base et de l'utilisateur PostgreSQL..."

# ==============================================================================
# Step 1 — PostgreSQL Role & Database Creation
# ==============================================================================
# Connects to the local PostgreSQL instance as the "postgres" superuser and
# executes an idempotent SQL block that:
#
#   a) Creates the application role (DB_USER) if it does not already exist,
#      with the specified password.
#   b) Creates the application database (DB_NAME) owned by DB_USER if it
#      does not already exist.
#   c) Grants ALL PRIVILEGES on the database, public schema, all existing
#      tables, and all existing sequences to DB_USER. This ensures the
#      application can create/alter/drop objects and read/write data.
#
# The DO $do$...$do$ blocks use PL/pgSQL anonymous blocks with existence
# checks to make the script safe to run multiple times without errors.
#
# NOTE: The `\c` meta-command switches the active connection to the newly
# created database so that schema-level GRANTs target the correct database.
# ==============================================================================
sudo -u postgres psql <<EOF
-- Create the application role if it does not already exist.
-- Uses a PL/pgSQL anonymous block because CREATE USER has no
-- IF NOT EXISTS clause in standard PostgreSQL.
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
   END IF;
END
\$do\$;

-- Create the application database if it does not already exist.
-- Ownership is assigned to DB_USER so it can manage its own schema.
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME') THEN
      CREATE DATABASE $DB_NAME OWNER $DB_USER;
   END IF;
END
\$do\$;

-- Grant database-level privileges (CONNECT, CREATE, TEMPORARY)
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;

-- Switch to the application database to issue schema-level grants
\c $DB_NAME

-- Grant full access to the public schema and all objects within it
GRANT ALL PRIVILEGES ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;
EOF

echo "✅ Base et utilisateur créés avec droits."

# ==============================================================================
# Step 2 — Alembic Database Migrations
# ==============================================================================
# Runs all pending Alembic migrations (from the current revision up to "head")
# to create or update the database schema (tables, indexes, constraints).
#
# PYTHONPATH is set to the current working directory so that Alembic's
# env.py can locate and import the application modules (app.database,
# app.models, etc.) regardless of the user's virtualenv configuration.
#
# If this is a fresh database, all migrations will run sequentially.
# On subsequent runs, only unapplied migrations execute (idempotent).
# ==============================================================================
echo "🚀 Lancement de Alembic upgrade..."
export PYTHONPATH=$(pwd)
alembic upgrade head
echo "✅ Migration Alembic terminée."

# ==============================================================================
# Step 3 — Seed Default Administrator Account
# ==============================================================================
# Executes an inline Python script that:
#
#   a) Opens a SQLAlchemy session via the application's SessionLocal factory.
#   b) Checks whether a user with the configured ADMIN_USERNAME already exists
#      using the application's CRUD helper.
#   c) If absent, creates a new admin user with is_admin=True and commits it.
#   d) If present, prints an informational message and skips creation.
#   e) Closes the database session to release the connection.
#
# The inline Python heredoc (<<END ... END) allows using the shell variables
# ($ADMIN_EMAIL, $ADMIN_USERNAME, $ADMIN_PASSWORD) directly inside the
# Python code via shell interpolation.
#
# NOTE: This approach relies on the app.crud.create_user function to handle
# password hashing internally. The plain-text password is never stored.
# ==============================================================================
echo "🚀 Création du compte admin initial..."

python3 - <<END
from app.database import SessionLocal
from app.crud import get_user_by_username, create_user
from app.models import UserCreate

# Open a new database session for the seeding operation
db = SessionLocal()

# Check for an existing admin to ensure idempotency
if get_user_by_username(db, "$ADMIN_USERNAME") is None:
    # Build the admin user data using the Pydantic schema
    admin_data = UserCreate(
        email="$ADMIN_EMAIL",
        username="$ADMIN_USERNAME",
        password="$ADMIN_PASSWORD"
    )
    # Persist the admin user with elevated privileges
    create_user(db, admin_data, is_admin=True)
    print("✅ Compte admin créé : $ADMIN_USERNAME / $ADMIN_PASSWORD")
else:
    print("ℹ️ Compte admin déjà existant.")

# Always close the session to return the connection to the pool
db.close()
END

echo "🎉 Setup terminé ! Tu peux maintenant lancer l'application FastAPI."
