# main.py
from fastapi import FastAPI
import logging

from app.database import create_tables
from app.predictor import plate_predictor
from app.routers import auth, predictions, admin, model

from app.crud import (
    get_user_by_email, get_user_by_username, create_user, authenticate_user,
    create_prediction, get_user_predictions
)

# ==================== Logging ====================
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# ==================== Application FastAPI ====================
app = FastAPI(
    title="LRS API",
    version="1.0.0",
    description="API Licence Plate Recognition (LRS).",
)

# ==================== Startup & Shutdown ====================
@app.on_event("startup")
async def startup_event():
    logger.info("🚀 Démarrage de l'API LRS")
    create_tables()
    logger.info("✅ Tables de base de données créées")

    # Charger le modèle au démarrage
    if plate_predictor.load_model():
        logger.info("✅ Modèle ML chargé avec succès")
    else:
        logger.error("❌ Modèle ML non chargé")

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("🛑 Arrêt de l'API LRS")

# ==================== Include Routers ====================
app.include_router(auth.router, prefix="/auth", tags=["Authentication"])
app.include_router(predictions.router, prefix="/predictions", tags=["Predictions"])
app.include_router(admin.router, prefix="/admin", tags=["Admin"])
app.include_router(model.router, prefix="/model", tags=["Model"])

@app.get("/", include_in_schema=False)
async def root():
    return {
        "message": "API LRS - Authentification requise",
        "documentation": "/docs",
        "version": "1.0.0"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True, log_level="info")
