"""
Script pour initialiser la base de données avec un utilisateur admin
"""
from sqlalchemy.orm import Session
from app.database import SessionLocal, create_tables, User
from app.auth import get_password_hash
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def init_database():
    """Initialise la base de données"""
    logger.info("🔄 Initialisation de la base de données...")

    # Créer les tables
    create_tables()

    # Créer un utilisateur admin par défaut
    db: Session = SessionLocal()

    try:
        # Vérifier si l'admin existe déjà
        existing_admin = db.query(User).filter(User.username == "admin").first()

        if existing_admin:
            logger.info("✅ L'utilisateur admin existe déjà")
        else:
            # Créer l'admin
            admin = User(
                email="admin@credit-scoring.com",
                username="admin",
                hashed_password=get_password_hash("admin123"),  # Changer en production !
                full_name="Administrator",
                is_active=True,
                is_admin=True
            )
            db.add(admin)
            db.commit()
            logger.info("✅ Utilisateur admin créé (username: admin, password: admin123)")

        # Créer un utilisateur de test
        existing_test = db.query(User).filter(User.username == "testuser").first()

        if not existing_test:
            test_user = User(
                email="test@example.com",
                username="testuser",
                hashed_password=get_password_hash("test123"),
                full_name="Test User",
                is_active=True,
                is_admin=False
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
