#!/bin/bash
# setup.sh : Setup complet de la base de données et Alembic pour Credit Scoring API

set -e

DB_NAME="credit_scoring_db"
DB_USER="credit_user"
DB_PASSWORD="credit_password"   # CHANGE-le pour la prod
ADMIN_EMAIL="admin@example.com"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="AdminP@ssw0rd"

echo "🚀 Création de la base et de l'utilisateur PostgreSQL..."

# 1️⃣ Création de l'utilisateur et de la base
sudo -u postgres psql <<EOF
-- Créer l'utilisateur si il n'existe pas
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
   END IF;
END
\$do\$;

-- Créer la base si elle n'existe pas
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME') THEN
      CREATE DATABASE $DB_NAME OWNER $DB_USER;
   END IF;
END
\$do\$;

-- Droits sur le schéma public
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
\c $DB_NAME
GRANT ALL PRIVILEGES ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;
EOF

echo "✅ Base et utilisateur créés avec droits."

# 2️⃣ Lancer Alembic pour créer les tables
echo "🚀 Lancement de Alembic upgrade..."
export PYTHONPATH=$(pwd)
alembic upgrade head
echo "✅ Migration Alembic terminée."

# 3️⃣ Créer un compte admin initial via script Python
echo "🚀 Création du compte admin initial..."

python3 - <<END
import os
from app.database import SessionLocal
from app.crud import get_user_by_username, create_user
from app.models import UserCreate

db = SessionLocal()

if get_user_by_username(db, "$ADMIN_USERNAME") is None:
    admin_data = UserCreate(
        email="$ADMIN_EMAIL",
        username="$ADMIN_USERNAME",
        password="$ADMIN_PASSWORD",
        full_name="Administrator"
    )
    create_user(db, admin_data, is_admin=True)
    print("✅ Compte admin créé : $ADMIN_USERNAME / $ADMIN_PASSWORD")
else:
    print("ℹ️ Compte admin déjà existant.")

db.close()
END

echo "🎉 Setup terminé ! Tu peux maintenant lancer l'application FastAPI."

