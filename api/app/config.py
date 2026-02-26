"""
config.py — Configuration centralisée de l'application SnapTaPlaque.

Ce module définit la classe de configuration ``Settings`` basée sur
Pydantic ``BaseSettings``, permettant de charger les paramètres de
l'application depuis les variables d'environnement ou un fichier
``.env``. Une instance singleton ``settings`` est exposée pour être
importée dans l'ensemble des modules de l'application.

Catégories de paramètres :
    - **Chemins**      — Répertoire racine du projet (``BASE_DIR``).
    - **Base de données** — URL de connexion PostgreSQL (``DATABASE_URL``).
    - **JWT**          — Clé secrète, algorithme et durée de validité
      des tokens d'accès (``SECRET_KEY``, ``ALGORITHM``,
      ``ACCESS_TOKEN_EXPIRE_MINUTES``).
    - **API**          — Métadonnées de l'API : titre, version,
      environnement d'exécution et mode debug (``API_TITLE``,
      ``API_VERSION``, ``API_ENV``, ``DEBUG``).
    - **Modèle**       — Configuration du pipeline de reconnaissance
      de plaques (``MODEL_CONFIG``).

Chargement des variables :
    Les valeurs par défaut définies dans la classe ``Settings`` peuvent
    être surchargées par des variables d'environnement portant le même
    nom (sensible à la casse) ou via un fichier ``.env`` situé à la
    racine du projet.

Version : 1.0.0
"""

from pathlib import Path
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """
    Configuration globale de l'application SnapTaPlaque.

    Hérite de ``BaseSettings`` (pydantic-settings) pour bénéficier du
    chargement automatique des variables d'environnement et de la
    validation de type à l'instanciation. Chaque attribut de classe
    représente un paramètre configurable avec sa valeur par défaut.

    Attributes:
        BASE_DIR (Path): Chemin absolu vers le répertoire racine du
            projet (parent du dossier ``app``). Calculé dynamiquement
            à partir de l'emplacement de ce fichier.
        DATABASE_URL (str): URL de connexion à la base de données
            PostgreSQL au format ``postgresql://user:password@host:port/db``.
        SECRET_KEY (str): Clé secrète utilisée pour signer les tokens
            JWT. Doit impérativement être modifiée en environnement de
            production.
        ALGORITHM (str): Algorithme de signature JWT. Par défaut
            ``"HS256"`` (HMAC-SHA256, algorithme symétrique).
        ACCESS_TOKEN_EXPIRE_MINUTES (int): Durée de validité des tokens
            d'accès en minutes. Par défaut ``30``.
        API_TITLE (str): Titre de l'API affiché dans la documentation
            OpenAPI (Swagger UI / ReDoc).
        API_VERSION (str): Version sémantique de l'API.
        API_ENV (str): Environnement d'exécution courant
            (``"development"``, ``"staging"``, ``"production"``).
        DEBUG (bool): Active le mode debug (logs détaillés, rechargement
            automatique). Doit être désactivé en production.
        MODEL_CONFIG (dict): Dictionnaire décrivant le pipeline de
            reconnaissance de plaques (nom, algorithme, version,
            fonctionnalités).
    """

    # ================== CHEMINS ==================

    # Chemin absolu vers le répertoire racine du projet, résolu
    # dynamiquement à partir de l'emplacement de ce fichier.
    BASE_DIR: Path = Path(__file__).resolve().parent.parent

    # ================== BASE DE DONNÉES ==================

    # URL de connexion PostgreSQL. En production, cette valeur doit
    # être définie via la variable d'environnement ``DATABASE_URL``
    # ou le fichier ``.env``.
    DATABASE_URL: str = "postgresql://plate_user:plate_password@localhost:5432/snaptaplaque_db"

    # ================== JWT ==================

    # Clé secrète pour la signature des tokens JWT. La valeur par défaut
    # est volontairement faible pour signaler qu'elle doit être remplacée
    # en environnement de production.
    SECRET_KEY: str = "your-secret-key-change-in-production"

    # Algorithme de signature JWT (HMAC-SHA256).
    ALGORITHM: str = "HS256"

    # Durée de validité des tokens d'accès, en minutes.
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30

    # ================== API ==================

    # Titre de l'API affiché dans la documentation OpenAPI.
    API_TITLE: str = "snapTaPlaque API"

    # Version sémantique de l'API.
    API_VERSION: str = "1.0.0"

    # Environnement d'exécution courant (development, staging, production).
    API_ENV: str = "development"

    # Mode debug : active les logs détaillés et le rechargement automatique.
    DEBUG: bool = True

    # ================== MODÈLE ==================

    # Configuration du pipeline de reconnaissance de plaques
    # d'immatriculation. Ce dictionnaire est accessible en lecture
    # par les endpoints d'information sur le modèle.
    MODEL_CONFIG: dict = {
        "name": "snapTaPlaque LPR Model",
        "algorithm": "YOLO + OCR",
        "version": "1.0",
        "features": [],
    }

    class Config:
        """
        Configuration interne de Pydantic BaseSettings.

        Attributes:
            env_file (str): Chemin vers le fichier ``.env`` contenant
                les variables d'environnement à charger.
            case_sensitive (bool): Si ``True``, les noms des variables
                d'environnement doivent correspondre exactement à la
                casse des attributs de la classe ``Settings``.
        """

        env_file = ".env"
        case_sensitive = True


# Instance singleton de la configuration, importée par les autres
# modules de l'application via ``from app.config import settings``.
settings = Settings()
