-- init-db.sql
-- Ce script s'exécute automatiquement au premier démarrage

-- Créer une extension si nécessaire
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Accorder tous les privilèges
GRANT ALL PRIVILEGES ON DATABASE snaptaplaque_db TO plate_user;

-- Message de confirmation
SELECT 'Base de données initialisée avec succès' AS status;
