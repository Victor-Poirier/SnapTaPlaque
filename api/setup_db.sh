#!/bin/bash
# Script pour créer l'utilisateur, la base et configurer les droits PostgreSQL

# Variables
DB_USER="credit_user"
DB_PASSWORD="credit_password"
DB_NAME="credit_scoring_db"

echo "🚀 Création de l'utilisateur et de la base PostgreSQL..."

# Créer l'utilisateur et la base via l'utilisateur postgres
sudo -u postgres psql <<EOF
-- Créer l'utilisateur s'il n'existe pas
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';
   END IF;
END
\$do\$;

-- Créer la base s'il n'existe pas
DO
\$do\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME') THEN
      CREATE DATABASE $DB_NAME OWNER $DB_USER;
   END IF;
END
\$do\$;

-- Donner tous les droits sur la base
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
EOF

echo "✅ Base et utilisateur créés avec succès !"

# Tester la connexion SQLAlchemy
echo "🔍 Test de connexion SQLAlchemy..."
python3 - <<PYTHON
from sqlalchemy import create_engine
try:
    engine = create_engine("postgresql://$DB_USER:$DB_PASSWORD@localhost:5432/$DB_NAME")
    conn = engine.connect()
    print("✅ Connexion OK")
    conn.close()
except Exception as e:
    print("❌ Connexion échouée:", e)
PYTHON

echo "🎯 Prêt pour lancer Alembic ou ton application FastAPI !"

