from fastapi import APIRouter, HTTPException
from app.predictor import plate_predictor
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.get("/info")
async def get_model_info():
    try:
        return {
            "loaded": plate_predictor.is_loaded(),
            "model_type": "YOLOv8 + EasyOCR",
            "pipeline": "LPRPipeline"
        }
    except Exception as e:
        logger.error(f"Erreur modèle: {str(e)}")
        raise HTTPException(status_code=500, detail="Cannot retrieve model info")
