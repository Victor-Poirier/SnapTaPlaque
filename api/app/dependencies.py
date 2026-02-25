# app/dependencies.py
from fastapi import Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.crud import get_user_by_username
from app.database import User

def get_current_user(db: Session = Depends(get_db), username: str = "admin") -> User:
    user = get_user_by_username(db, username=username)
    if not user or not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")
    return user

def get_current_admin_user(current_user: User = Depends(get_current_user)) -> User:
    if not current_user.is_admin:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not an admin")
    return current_user

