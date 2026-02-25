from fastapi import APIRouter, HTTPException
from app.predictor import predictor
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.get("/info")
async def get_model_info():
    try:
        return predictor.get_model_info()
    except Exception as e:
        logger.error(f"Erreur modèle: {str(e)}")
        raise HTTPException(status_code=500, detail="Cannot retrieve model info")

