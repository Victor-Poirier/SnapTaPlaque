from app.database import SessionLocal, create_tables, User
from app.auth import get_password_hash

def create_admin():
    db = SessionLocal()
    try:
        if db.query(User).filter(User.username=="admin").first():
            print("Admin already exists ✅")
            return

        admin_user = User(
            username="admin",
            email="admin@example.com",
            hashed_password=get_password_hash("AdminP@ssw0rd"),
            is_admin=True,
            is_active=True
        )
        db.add(admin_user)
        db.commit()
        print("🚀 Admin created successfully")
    finally:
        db.close()

if __name__ == "__main__":
    create_tables()
    create_admin()