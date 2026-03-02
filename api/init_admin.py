"""
init_admin.py — Bootstrap script for creating the default admin user.

This standalone script initialises the database schema (if not already present)
and seeds a default administrator account into the SnapTaPlaque PostgreSQL
database. It is intended to be run once during initial deployment or after
a fresh database reset.

Usage:
    python init_admin.py

Behaviour:
    1. Calls ``create_tables()`` to ensure all SQLAlchemy-mapped tables exist
       in the database (idempotent — safe to call multiple times).
    2. Checks whether a user with the username "admin" already exists.
       - If yes -> prints a confirmation message and exits without changes.
       - If no -> creates a new admin user with pre-defined credentials and
         commits it to the database.

Default admin credentials (change immediately in production):
    - Username : admin
    - Email    : admin@example.com
    - Password : AdminP@ssw0rd

WARNING:
    This script contains hard-coded credentials. In a production environment,
    replace them with values sourced from environment variables or a secrets
    manager. Never commit real passwords to version control.

NOTE:
    If Alembic is managing schema migrations, consider removing the
    ``create_tables()`` call and running ``alembic upgrade head`` instead
    to avoid conflicts between Alembic's version tracking and direct DDL.
"""

from app.database import SessionLocal, create_tables, User
from app.auth import get_password_hash


def create_admin():
    """Create a default administrator account if one does not already exist.

    Opens a new database session, checks for an existing user named "admin",
    and inserts one with elevated privileges when absent. The session is
    always closed in the ``finally`` block to prevent connection leaks.

    Raises:
        sqlalchemy.exc.SQLAlchemyError: If a database-level error occurs
            during the query or commit (e.g., connection refused, constraint
            violation).
    """
    db = SessionLocal()
    try:
        # Check for an existing admin to make the script idempotent
        if db.query(User).filter(User.username == "admin").first():
            print("Admin already exists ✅")
            return

        # Build the admin user with hashed password and elevated flags
        admin_user = User(
            username="admin",
            email="admin@example.com",
            hashed_password=get_password_hash("AdminP@ssw0rd"),
            is_admin=True,
            is_active=True,
        )
        db.add(admin_user)
        db.commit()
        print("🚀 Admin created successfully")
    finally:
        # Ensure the session is closed regardless of success or failure
        db.close()


if __name__ == "__main__":
    # Ensure all tables exist before attempting to insert the admin user
    create_tables()
    create_admin()