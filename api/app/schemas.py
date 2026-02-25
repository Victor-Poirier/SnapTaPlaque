from pydantic import BaseModel, EmailStr, Field
from typing import Optional, Dict, Any, List
from datetime import datetime


# ---------- AUTH ----------
class LoginRequest(BaseModel):
    username: str
    password: str


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"


# ================= PREDICTIONS =================

class PlateHistory(BaseModel):
    id: int
    filename: str
    results: Dict[str, Any]
    created_at: datetime

    class Config:
        from_attributes = True


class PlateStats(BaseModel):
    total_predictions: int


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
