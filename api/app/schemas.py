from pydantic import BaseModel, EmailStr, Field
from typing import Optional, Literal
from datetime import datetime


# ---------- AUTH ----------
class LoginRequest(BaseModel):
    username: str
    password: str


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"


# ---------- CREDIT ----------
class CreditRequest(BaseModel):
    age: int = Field(..., ge=18, le=100)
    income: float = Field(..., gt=0)
    credit_amount: float = Field(..., gt=0)
    duration: int = Field(..., ge=6, le=120)


class CreditResponse(BaseModel):
    decision: Literal["APPROVED", "REJECTED"]
    probability: float
    model_ver: str
    prediction_id: int


# ================= PREDICTIONS =================


class PredictionHistory(BaseModel):
    id: int
    age: int
    income: float
    credit_amount: float
    duration: int
    decision: str
    probability: float
    model_version: str
    created_at: datetime

    class Config:
        from_attributes = True


class PredictionStats(BaseModel):
    total_predictions: int
    approved: int
    rejected: int
    approval_rate: float


# ---------- USER ----------
class UserCreate(BaseModel):
    email: EmailStr
    username: str
    password: str
    full_name: Optional[str] = None
    is_admin: bool = False


class UserResponse(BaseModel):
    id: int
    email: str
    username: str
    full_name: Optional[str]
    is_active: bool
    is_admin: bool
    created_at: datetime

    class Config:
        from_attributes = True

