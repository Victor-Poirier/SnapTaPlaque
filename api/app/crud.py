from sqlalchemy.orm import Session
from sqlalchemy import func
from app.database import User, Prediction
from app.models import UserCreate
from sqlalchemy.orm import Session

from app import models
from app.security import get_password_hash

from typing import Optional


def get_user_by_email(db: Session, email: str) -> User:
    return db.query(User).filter(User.email == email).first()

def get_user_by_username(db: Session, username: str) -> User:
    return db.query(User).filter(User.username == username).first()

def create_user(
    db: Session,
    user: UserCreate,
):
    hashed_password = get_password_hash(user.password)

    db_user = User(
        email=user.email,
        username=user.username,
        hashed_password=hashed_password,
        name=user.name,
        first_name=user.first_name,
        is_admin=user.is_admin
    )

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def authenticate_user(db: Session, username: str, password: str):
    user = get_user_by_username(db, username)
    if not user:
        return None
    from app.auth import verify_password
    if not verify_password(password, user.hashed_password):
        return None
    return user


#######################################################################
# A Modifier en dessous

def create_prediction(
    db: Session,
    user_id: int,
    age: int,
    income: float,
    credit_amount: float,
    duration: int,
    decision: str,
    probability: float,
    model_version: str,
    ip_address: Optional[str] = None
) -> Prediction:
    db_prediction = Prediction(
        user_id=user_id,
        age=age,
        income=income,
        credit_amount=credit_amount,
        duration=duration,
        decision=decision,
        probability=probability,
        model_version=model_version,
        ip_address=ip_address
    )
    db.add(db_prediction)
    db.commit()
    db.refresh(db_prediction)
    return db_prediction

def get_user_predictions(db: Session, user_id: int, skip: int = 0, limit: int = 100):
    return db.query(Prediction).filter(Prediction.user_id == user_id).order_by(Prediction.created_at.desc()).offset(skip).limit(limit).all()



def get_user_prediction_stats(db: Session, user_id: int) -> dict:
    """
    Statistiques des prédictions pour un utilisateur donné
    """

    # Total des prédictions
    total = db.query(func.count(Prediction.id)) \
              .filter(Prediction.user_id == user_id) \
              .scalar()

    # Nombre de crédits approuvés
    approved = db.query(func.count(Prediction.id)) \
                 .filter(
                     Prediction.user_id == user_id,
                     Prediction.decision == "APPROVED"
                 ) \
                 .scalar()

    # Nombre de crédits rejetés
    rejected = db.query(func.count(Prediction.id)) \
                 .filter(
                     Prediction.user_id == user_id,
                     Prediction.decision == "REJECTED"
                 ) \
                 .scalar()

    approval_rate = (approved / total) if total > 0 else 0.0

    return {
        "total_predictions": total,
        "approved": approved,
        "rejected": rejected,
        "approval_rate": round(approval_rate, 3),
    }

#######################################################################

def get_all_users(db: Session):
    return db.query(User).all()

def get_global_stats(db: Session) -> dict:
    total_users = db.query(func.count(User.id)).scalar()
    total_predictions = db.query(func.count(Prediction.id)).scalar()

    return {
        "total_users": total_users,
        "total_predictions": total_predictions,
    }


