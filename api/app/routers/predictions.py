from fastapi import APIRouter, Depends, HTTPException, Request
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db, User
from app.schemas import (
    CreditRequest,
    CreditResponse,
    PredictionHistory,
    PredictionStats,
)

from app.auth import get_current_active_user
from app.crud import create_prediction, get_user_predictions, get_user_prediction_stats
from app.predictor import predictor

router = APIRouter()


@router.post("/predict", response_model=CreditResponse)
async def predict_credit(request: CreditRequest, http_request: Request,
                         current_user: User = Depends(get_current_active_user),
                         db: Session = Depends(get_db)):
    if not predictor.is_loaded():
        raise HTTPException(status_code=500, detail="Model not available")

    decision, probability = predictor.predict(
        age=request.age,
        income=request.income,
        credit_amount=request.credit_amount,
        duration=request.duration
    )

    db_prediction = create_prediction(
        db=db,
        user_id=current_user.id,
        age=request.age,
        income=request.income,
        credit_amount=request.credit_amount,
        duration=request.duration,
        decision=decision,
        probability=probability,
        model_version=f"v{predictor.model_config['version']}",
        ip_address=http_request.client.host if http_request.client else None
    )

    # Retour de la réponse (CORRECTION ICI : model_ver au lieu de model_version)
    return CreditResponse(
        decision=decision,
        probability=round(probability, 4),
        model_ver=f"credit_scoring_model_v{predictor.model_config['version']}",
        prediction_id=db_prediction.id
    )


@router.get("/history", response_model=List[PredictionHistory])
async def get_prediction_history(skip: int = 0, limit: int = 100,
                                 current_user: User = Depends(get_current_active_user),
                                 db: Session = Depends(get_db)):
    return get_user_predictions(db, current_user.id, skip=skip, limit=limit)


@router.get("/stats", response_model=PredictionStats)
async def get_prediction_statistics(current_user: User = Depends(get_current_active_user),
                                    db: Session = Depends(get_db)):
    return get_user_prediction_stats(db, current_user.id)