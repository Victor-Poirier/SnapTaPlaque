"""
init_bd.py — Database initialization and seed script for SnapTaPlaque.

This script bootstraps the PostgreSQL database by creating all
SQLAlchemy-mapped tables and seeding default user accounts (an administrator
and a test user) as well as sample vehicles. It is designed to be run once
during initial deployment or after a complete database reset.

Usage::

    python init_bd.py

Behaviour:
    1. Creates all tables defined in the SQLAlchemy ``Base.metadata`` via
       ``create_tables()`` (idempotent — safe to call multiple times).
    2. Seeds a default **admin** account if one does not already exist.
    3. Seeds a default **test user** account if one does not already exist.
    4. Seeds sample **vehicles** if they do not already exist.
    5. Rolls back the current transaction on any unexpected error to leave
       the database in a consistent state.

Default credentials (change immediately in production):
    +-----------+----------+-----------+
    | Username  | Password | Role      |
    +-----------+----------+-----------+
    | admin     | admin123 | Admin     |
    | testuser  | test123  | Standard  |
    +-----------+----------+-----------+

WARNING:
    This script contains **hard-coded credentials** for convenience during
    development. In a production environment, replace them with values
    sourced from environment variables or a secrets manager. Never ship
    default passwords to production.

NOTE:
    If Alembic is managing schema migrations, consider removing the
    ``create_tables()`` call and running ``alembic upgrade head`` instead
    to avoid conflicts between Alembic's revision tracking and direct DDL
    issued by ``Base.metadata.create_all()``.
"""

from sqlalchemy.orm import Session
from app.database import SessionLocal, create_tables, User, Vehicle
from app.auth import get_password_hash
import logging

# Configure the root logger to INFO so that progress messages are visible
# on stdout during the initialisation process.
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Sample vehicles for testing purposes
SAMPLE_VEHICLES = [
    {
        "license_plate": "AB-123-CD",
        "brand": "Peugeot",
        "model": "308",
        "year": 2021,
        "color": "Gris",
        "engine": "1.5 BlueHDi 130",
        "trim": "Allure",
    }
]

def init_database():
    """Initialise the database schema and seed default user accounts and vehicles.

    This function performs the following steps in order:

    1. **Table creation** — ensures every SQLAlchemy model is materialised
       as a database table (no-op if the tables already exist).
    2. **Admin seeding** — inserts an ``admin`` user with elevated
       privileges (``is_admin=True``) unless one already exists.
    3. **Test user seeding** — inserts a ``testuser`` account intended for
       development / QA use unless one already exists.
    4. **Vehicle seeding** — inserts sample vehicles for testing unless
       they already exist.

    Each seed step is idempotent: running the function multiple times will
    never create duplicate rows.

    Raises:
        Exception: Any unexpected database error is caught, logged, and
            the transaction is rolled back. The exception is **not**
            re-raised, allowing the process to exit gracefully.
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

        # ----------------------------------------------------------------------
        # Seed: Sample vehicles
        # ----------------------------------------------------------------------
        vehicles_added = 0
        for vehicle_data in SAMPLE_VEHICLES:
            existing = (
                db.query(Vehicle)
                .filter(Vehicle.license_plate == vehicle_data["license_plate"])
                .first()
            )
            if not existing:
                vehicle = Vehicle(**vehicle_data)
                db.add(vehicle)
                vehicles_added += 1

        if vehicles_added > 0:
            db.commit()
            logger.info(f"✅ {vehicles_added} véhicule(s) de test ajouté(s)")
        else:
            logger.info("✅ Tous les véhicules de test existent déjà")

        logger.info("✅ Base de données initialisée avec succès")

    except Exception as e:
        logger.error(f"❌ Erreur lors de l'initialisation : {str(e)}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    init_database()
