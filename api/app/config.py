"""
Configuration de l'application avec gestion des variables d'environnement
"""
from pathlib import Path
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Configuration de l'application"""

    # Chemins
    BASE_DIR: Path = Path(__file__).resolve().parent.parent

    # Database
    DATABASE_URL: str = "postgresql://plate_user:plate_password@localhost:5432/snaptaplaque_db"

    # JWT
    SECRET_KEY: str = "your-secret-key-change-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # API
    API_TITLE: str = "snapTaPlaque API"
    API_VERSION: str = "1.0.0"
    API_ENV: str = "development"
    DEBUG: bool = True

    # Model
    MODEL_CONFIG: dict = {
        "name": "snapTaPlaque LPR Model",
        "algorithm": "YOLO + OCR",
        "version": "1.0",
        "features": [],
    }

    class Config:
        env_file = ".env"
        case_sensitive = True


# Instance globale
settings = Settings()
