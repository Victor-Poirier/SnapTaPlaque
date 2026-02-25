from datetime import datetime, timedelta
from typing import Optional

from jose import jwt, JWTError
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from passlib.context import CryptContext

from app.database import get_db


from app import crud, models, schemas,database
from app.config import settings

# ================== CONFIG ==================

SECRET_KEY = settings.SECRET_KEY
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# ================== PASSWORD ==================

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)

# ================== JWT ==================

def create_access_token(
    data: dict,
    expires_delta: Optional[timedelta] = None
) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def decode_access_token(token: str) -> dict:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalide ou expiré",
        )

# ================== DEPENDANCES ==================

def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> database.User:
    payload = decode_access_token(token)
    username: str | None = payload.get("sub")

    if username is None:
        raise HTTPException(status_code=401, detail="Token invalide")

    user = crud.get_user_by_username(db, username)
    if not user:
        raise HTTPException(status_code=401, detail="Utilisateur non trouvé")

    return user

def get_current_active_user(
    current_user: database.User = Depends(get_current_user),
) -> database.User:
    if not current_user.is_active:
        raise HTTPException(status_code=403, detail="Compte désactivé")
    return current_user

