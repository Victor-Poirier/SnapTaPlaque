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


class Settings(BaseSettings):
    """
    Configuration globale de l'application SnapTaPlaque.

    Hérite de ``BaseSettings`` (pydantic-settings) pour bénéficier du
    chargement automatique des variables d'environnement et de la
    validation de type à l'instanciation. Chaque attribut de classe
    représente un paramètre configurable avec sa valeur par défaut.

    :ivar BASE_DIR: Chemin absolu vers le répertoire racine du
        projet (parent du dossier ``app``). Calculé dynamiquement
        à partir de l'emplacement de ce fichier.
    :vartype BASE_DIR: pathlib.Path
    :ivar DATABASE_URL: URL de connexion à la base de données
        PostgreSQL au format ``postgresql://user:password@host:port/db``.
    :vartype DATABASE_URL: str
    :ivar SECRET_KEY: Clé secrète utilisée pour signer les tokens
        JWT. Doit impérativement être modifiée en environnement de
        production.
    :vartype SECRET_KEY: str
    :ivar ALGORITHM: Algorithme de signature JWT. Par défaut
        ``"HS256"`` (HMAC-SHA256, algorithme symétrique).
    :vartype ALGORITHM: str
    :ivar ACCESS_TOKEN_EXPIRE_MINUTES: Durée de validité des tokens
        d'accès en minutes. Par défaut ``30``.
    :vartype ACCESS_TOKEN_EXPIRE_MINUTES: int
    :ivar API_TITLE: Titre de l'API affiché dans la documentation
        OpenAPI (Swagger UI / ReDoc).
    :vartype API_TITLE: str
    :ivar API_DESCRIPTION: Courte description de l'API ajoutée à la
        documentation OpenAPI.
    :vartype API_DESCRIPTION: str
    :ivar API_VERSION: Version sémantique de l'API.
    :vartype API_VERSION: str
    :ivar API_ENV: Environnement d'exécution courant
        (``"development"``, ``"staging"``, ``"production"``).
    :vartype API_ENV: str
    :ivar DEBUG: Active le mode debug (logs détaillés, rechargement
        automatique). Doit être désactivé en production.
    :vartype DEBUG: bool
    :ivar MODEL_CONFIG: Dictionnaire décrivant le pipeline de
        reconnaissance de plaques (nom, algorithme, version,
        fonctionnalités).
    :vartype MODEL_CONFIG: dict
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
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10

    # ================== API ==================

    # Titre de l'API affiché dans la documentation OpenAPI.
    API_TITLE: str = "snapTaPlaque API"

    API_DESCRIPTION: str = "API Licence Plate Recognition (LRS)."

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
        "algorithm": "YOLO ONNX (HuggingFace) + EasyOCR",
        "version": "1.0",
    }

    class Config:
        """
        Configuration interne de Pydantic BaseSettings.

        :ivar env_file: Chemin vers le fichier ``.env`` contenant
            les variables d'environnement à charger.
        :vartype env_file: str
        :ivar case_sensitive: Si ``True``, les noms des variables
            d'environnement doivent correspondre exactement à la
            casse des attributs de la classe ``Settings``.
        :vartype case_sensitive: bool
        """

        env_file = ".env"
        case_sensitive = True


# Instance singleton de la configuration, importée par les autres
# modules de l'application via ``from app.config import settings``.
settings = Settings()
