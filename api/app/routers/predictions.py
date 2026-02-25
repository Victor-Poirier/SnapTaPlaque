from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db, User
from app.auth import get_current_active_user
from app.crud import create_prediction, get_user_predictions, get_user_prediction_stats
from app.predictor import plate_predictor
from app.schemas import PlateHistory, PlateStats

import cv2
import numpy as np

router = APIRouter()


@router.post("/predict")
async def predict_plate(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    contents = await file.read()

    if not plate_predictor.is_loaded():
        raise HTTPException(status_code=503, detail="Modèle non chargé")

    try:
        result = plate_predictor.predict(contents)
    except ValueError:
        raise HTTPException(status_code=400, detail="Image invalide")

    results = [result] if result["plate_text"] else []

    prediction = create_prediction(
        db=db,
        user_id=current_user.id,
        filename=file.filename,
        results=results
    )

    return {
        "filename": file.filename,
        "results": results,
        "prediction_id": prediction.id
    }


@router.get("/history", response_model=List[PlateHistory])
async def get_prediction_history(
    skip: int = 0,
    limit: int = 100,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    return get_user_predictions(db, current_user.id, skip=skip, limit=limit)


@router.get("/stats", response_model=PlateStats)
async def get_prediction_statistics(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    return get_user_prediction_stats(db, current_user.id)
