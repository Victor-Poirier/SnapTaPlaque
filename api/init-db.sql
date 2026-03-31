-- ==============================================================================
-- init-db.sql — Script d'initialisation de la base de données pour SnapTaPlaque
-- ==============================================================================
-- Ce script SQL est exécuté automatiquement par PostgreSQL lors du tout premier
-- démarrage du conteneur, lorsque le volume de la base de données est vide et
-- que la base est créée pour la première fois.
--
-- Il est généralement monté dans le conteneur PostgreSQL via un mapping de
-- volume Docker vers le répertoire /docker-entrypoint-initdb.d/ :
--
--   volumes:
--     - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
--
-- PostgreSQL exécute tous les fichiers .sql (et .sh) trouvés dans ce répertoire
-- par ordre alphabétique durant la phase initiale `initdb` uniquement. Lors des
-- redémarrages ultérieurs (quand le volume de données existe déjà), ce script
-- n'est PAS ré-exécuté.
--
-- Ce que fait ce script :
--   1. Active l'extension uuid-ossp (fournit des fonctions de génération de UUID
--      telles que uuid_generate_v4(), utile si des UUID sont un jour requis
--      comme clés primaires).
--   2. Accorde tous les privilèges sur la base de données snaptaplaque_db au rôle
--      plate_user, garantissant que l'application dispose des permissions
--      nécessaires pour créer des tables, insérer des données, exécuter des
--      migrations, etc.
--   3. Affiche un message de confirmation dans les logs de démarrage de PostgreSQL.
--
-- Prérequis :
--   - La base de données "snaptaplaque_db" et le rôle "plate_user" doivent
--     déjà exister. Ils sont créés automatiquement par les variables d'environnement
--     POSTGRES_DB / POSTGRES_USER définies dans docker-compose.yml.
--
-- NOTE : La création du schéma (tables, index, contraintes) est gérée par les
--        migrations Alembic ou au démarrage de l'app, et NON par ce script.
--        Ce fichier effectue uniquement la configuration de base, au niveau SGBD.
-- ==============================================================================

-- Activation de l'extension uuid-ossp pour les fonctions de génération de UUID.
-- IF NOT EXISTS assure l'idempotence (pas d'erreur si déjà activée).
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Octroi de tous les privilèges sur la base de données snaptaplaque_db à plate_user.
-- Cela inclut CREATE, CONNECT, TEMPORARY, et permet à l'utilisateur de
-- créer et gérer des schémas, tables et autres objets de la base de données.
GRANT ALL PRIVILEGES ON DATABASE snaptaplaque_db TO plate_user;

-- Affiche un message de confirmation dans les logs du serveur PostgreSQL pour
-- que les opérateurs puissent vérifier que le script d'initialisation s'est
-- exécuté avec succès.
SELECT 'Base de données initialisée avec succès' AS status;
