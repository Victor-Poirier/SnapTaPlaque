from pydantic import BaseModel, Field, EmailStr, field_validator
from typing import Literal, Optional
from datetime import datetime


# -------------------- SCHÉMAS CREDIT --------------------
class CreditRequest(BaseModel):
    age: int = Field(..., ge=18, le=100, description="Âge du demandeur")
    income: float = Field(..., gt=0, description="Revenu mensuel en euros")
    credit_amount: float = Field(..., gt=0, description="Montant du crédit demandé")
    duration: int = Field(..., ge=6, le=120, description="Durée du crédit en mois")

    model_config = {
        "json_schema_extra": {
            "example": {
                "age": 35,
                "income": 3200.0,
                "credit_amount": 15000.0,
                "duration": 48
            }
        }
    }


class CreditResponse(BaseModel):
    decision: Literal["APPROVED", "REJECTED"]
    probability: float
    model_ver: str
    prediction_id: Optional[int] = None

    model_config = {
        "json_schema_extra": {
            "example": {
                "decision": "APPROVED",
                "probability": 0.82,
                "model_ver": "credit_scoring_model_v1.0",
                "prediction_id": 123
            }
        },
        "protected_namespaces": ()
    }


class UserStats(BaseModel):
    """
    Statistiques globales d'un utilisateur (admin)
    """
    user_id: int
    username: str
    total_predictions: int
    approved: int
    rejected: int
    approval_rate: float


# -------------------- SCHÉMAS UTILISATEUR --------------------
class UserCreate(BaseModel):
    email: EmailStr
    username: str = Field(..., min_length=3, max_length=50)
    password: str = Field(..., min_length=8)
    full_name: Optional[str] = None

    @field_validator("username")
    @classmethod
    def validate_username(cls, v):
        if not v.isalnum():
            raise ValueError("Username must be alphanumeric")
        return v

    model_config = {
        "json_schema_extra": {
            "example": {
                "email": "user@example.com",
                "username": "johndoe",
                "password": "SecureP@ssw0rd",
                "full_name": "John Doe"
            }
        }
    }


class UserResponse(BaseModel):
    id: int
    email: str
    username: str
    full_name: Optional[str]
    is_active: bool
    is_admin: bool
    created_at: datetime

    model_config = {"from_attributes": True}


class Token(BaseModel):
    """Schéma pour le token JWT (simple)"""
    access_token: str
    token_type: str = "bearer"


class TokenWithRefresh(Token):
    """Schéma pour le token JWT avec refresh token"""
    refresh_token: str

    class Config:
        json_schema_extra = {
            "example": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "bearer",
                "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
        }


class TokenData(BaseModel):
    """Données contenues dans le token"""
    username: Optional[str] = None
    token_type: Optional[str] = None  # "access" ou "refresh"

