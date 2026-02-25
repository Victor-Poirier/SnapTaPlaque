from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Boolean, ForeignKey, Index
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime
from app.config import settings

engine = create_engine(
    settings.DATABASE_URL,
    echo=settings.DEBUG,
    pool_pre_ping=True,
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)

    ####################
    name = Column(String, nullable=True)
    first_name = Column(String, nullable=True)
    ####################

    is_active = Column(Boolean, default=True)
    is_admin = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    predictions = relationship("Prediction", back_populates="user")


"""
A modifier en dessous pour adapter a notre modèle LPR
"""

class Prediction(Base):
    __tablename__ = "predictions"

    # user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    # id = Column(Integer, primary_key=True, index=True)
    # age = Column(Integer, nullable=False)
    # income = Column(Float, nullable=False)
    # credit_amount = Column(Float, nullable=False)
    # duration = Column(Integer, nullable=False)
    # decision = Column(String, nullable=False)
    # probability = Column(Float, nullable=False)
    # model_version = Column(String, nullable=False)
    # created_at = Column(DateTime, default=datetime.utcnow)
    # ip_address = Column(String, nullable=True)

    user = relationship("User", back_populates="predictions")

    __table_args__ = (
        Index("idx_user_created", "user_id", "created_at"),
    )

def create_tables():
    Base.metadata.create_all(bind=engine)
    print("✅ Tables créées avec succès")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

