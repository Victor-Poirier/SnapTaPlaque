"""
init_bd.py — Script d'initialisation et de peuplement de la base de données pour SnapTaPlaque.

Ce script amorce la base de données PostgreSQL en créant toutes les tables
définies par les modèles SQLAlchemy et en insérant des comptes utilisateurs
par défaut (un administrateur et un utilisateur de test) ainsi que des véhicules
d'exemple. Il est conçu pour être exécuté une seule fois lors du déploiement
initial ou après une réinitialisation complète de la base de données.

Utilisation ::

    python init_bd.py

Comportement :
    1. Crée toutes les tables définies dans ``Base.metadata`` via
       ``create_tables()`` (idempotent — peut être appelé plusieurs fois sans danger).
    2. Insère un compte **admin** par défaut s'il n'existe pas déjà.
    3. Insère un compte **testuser** par défaut s'il n'existe pas déjà.
    4. Annule (rollback) la transaction courante en cas d'erreur inattendue afin de
       laisser la base de données dans un état cohérent.

Identifiants par défaut (à modifier immédiatement en production) :
    +-----------+----------+---------------+
    | Username  | Password | Rôle          |
    +-----------+----------+---------------+
    | admin     | admin123 | Administrateur|
    | testuser  | test123  | Standard      |
    +-----------+----------+---------------+

.. warning::
    Ce script contient des **identifiants codés en dur** pour des raisons de 
    commodité lors du développement. Dans un environnement de production, 
    remplacez-les par des valeurs extraites de variables d'environnement ou d'un 
    gestionnaire de secrets. Ne déployez jamais de mots de passe par défaut en production.

.. note::
    Si Alembic gère les migrations de schéma, envisagez de supprimer l'appel à
    ``create_tables()`` et d'exécuter ``alembic upgrade head`` à la place pour
    éviter les conflits entre le suivi des révisions d'Alembic et les commandes
    DDL directes émises par ``Base.metadata.create_all()``.
"""

from sqlalchemy.orm import Session
from app.database import SessionLocal, create_tables, User, Vehicle
from app.auth import get_password_hash
import logging

# Configure the root logger to INFO so that progress messages are visible
# on stdout during the initialisation process.
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_database():
    """
    Initialiser le schéma de la base de données et insérer les comptes par défaut.

    Cette fonction effectue les étapes suivantes dans l'ordre :

    1. **Création des tables** — s'assure que chaque modèle SQLAlchemy est matérialisé
       sous forme de table dans la base de données (aucune action si elles existent déjà).
    2. **Création de l'admin** — insère un utilisateur ``admin`` avec des privilèges
       élevés (``is_admin=True``) à moins qu'il n'en existe déjà un.
    3. **Création de l'utilisateur de test** — insère un compte ``testuser`` destiné 
       au développement / tests QA, à moins qu'il n'en existe déjà un.

    Chaque étape d'insertion est idempotente : exécuter la fonction plusieurs fois
    ne créera jamais de doublons.

    :raises Exception: Toute erreur inattendue de la base de données est interceptée,
                       journalisée, et la transaction est annulée. L'exception n'est
                       **pas** renvoyée, ce qui permet au processus de se terminer proprement.
    """
    logger.info("🔄 Initialisation de la base de données...")

    # Ensure all ORM-mapped tables exist in the target database
    create_tables()

    # Open a scoped session for the seeding operations
    db: Session = SessionLocal()

    try:
        # ----------------------------------------------------------------------
        # Seed: Administrator account
        # ----------------------------------------------------------------------
        existing_admin = db.query(User).filter(User.username == "admin").first()

        if existing_admin:
            logger.info("✅ L'utilisateur admin existe déjà")
        else:
            admin = User(
                email="admin@credit-scoring.com",
                username="admin",
                hashed_password=get_password_hash("admin123"),
                full_name="Administrator",
                is_active=True,
                is_admin=True,
            )
            db.add(admin)
            db.commit()
            logger.info("✅ Utilisateur admin créé (username: admin, password: admin123)")

        # ----------------------------------------------------------------------
        # Seed: Test / QA user account
        # ----------------------------------------------------------------------
        existing_test = db.query(User).filter(User.username == "testuser").first()

        if not existing_test:
            test_user = User(
                email="test@example.com",
                username="testuser",
                hashed_password=get_password_hash("test123"),
                full_name="Test User",
                is_active=True,
                is_admin=False,
            )
            db.add(test_user)
            db.commit()
            logger.info("✅ Utilisateur de test créé (username: testuser, password: test123)")

        logger.info("✅ Base de données initialisée avec succès")

    except Exception as e:
        logger.error(f"❌ Erreur lors de l'initialisation : {str(e)}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    init_database()
