from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import get_current_admin_user
from app.crud import get_all_users, get_global_stats
from app.schemas import UserResponse

router = APIRouter()

@router.get("/users", response_model=list[UserResponse])
def list_users(
    db: Session = Depends(get_db),
    admin=Depends(get_current_admin_user),
):
    return get_all_users(db)

@router.get("/stats")
def global_stats(
    db: Session = Depends(get_db),
    admin=Depends(get_current_admin_user),
):
    return get_global_stats(db)

