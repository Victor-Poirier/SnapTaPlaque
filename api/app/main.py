"""
main.py — Point d'entrée de l'application FastAPI SnapTaPlaque.

Ce module constitue le point d'entrée principal de l'API SnapTaPlaque.
Il initialise l'application FastAPI, configure le système de
journalisation, enregistre les routeurs (authentification, prédictions,
administration, modèle), et définit les événements de cycle de vie
(démarrage et arrêt) de l'application.

Composants exposés :
    - ``app``              — Instance principale de l'application
      FastAPI, point d'entrée ASGI pour le serveur Uvicorn.
    - ``startup_event``    — Gestionnaire d'événement exécuté au
      démarrage de l'application, responsable du chargement du modèle
      de reconnaissance de plaques.
    - ``shutdown_event``   — Gestionnaire d'événement exécuté à l'arrêt
      de l'application, responsable de la journalisation de l'arrêt.
    - ``root``             — Endpoint racine (``GET /``) retournant un
      message d'accueil avec les informations de base de l'API.

Routeurs enregistrés :
    - ``/auth``            — Endpoints d'authentification (inscription,
      connexion, gestion des tokens JWT).
    - ``/predictions``     — Endpoints de soumission et de consultation
      des prédictions de reconnaissance de plaques.
    - ``/admin``           — Endpoints d'administration (gestion des
      utilisateurs, statistiques globales).
    - ``/model``           — Endpoints d'information sur le modèle de
      reconnaissance de plaques.

Cycle de vie :
    - **Démarrage** — Le modèle de reconnaissance de plaques est chargé
      en mémoire via ``plate_predictor.load_model()``. Un message de
      succès ou d'erreur est journalisé selon le résultat du chargement.
    - **Arrêt** — Un message de journalisation est émis pour signaler
      l'arrêt propre de l'API.

Version : 1.0.0
"""

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

# Configuration globale du système de journalisation. Le niveau
# ``INFO`` est utilisé par défaut pour journaliser les événements
# significatifs (démarrage, arrêt, chargement du modèle) sans
# surcharger la sortie avec les messages de debug.
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

# Logger dédié au module ``main``, utilisé pour les messages de
# cycle de vie de l'application (démarrage, arrêt, chargement du
# modèle).
logger = logging.getLogger(__name__)

# ==================== Application FastAPI ====================

# Instance principale de l'application FastAPI. Les métadonnées
# ``title``, ``version`` et ``description`` sont utilisées pour
# générer automatiquement la documentation OpenAPI (Swagger UI /
# ReDoc) accessible aux endpoints ``/docs`` et ``/redoc``.
app = FastAPI(
    title="LRS API",
    version="1.0.0",
    description="API Licence Plate Recognition (LRS).",
)


# ==================== Startup & Shutdown ====================

@app.on_event("startup")
async def startup_event():
    """
    Initialiser les ressources de l'application au démarrage.

    Gestionnaire d'événement FastAPI exécuté une seule fois lors du
    démarrage du serveur ASGI. Il charge le modèle de reconnaissance
    de plaques en mémoire via ``plate_predictor.load_model()`` et
    journalise le résultat de l'opération.

    En cas d'échec du chargement du modèle, l'application démarre
    malgré tout mais les endpoints de prédiction ne seront pas
    fonctionnels tant que le modèle n'aura pas été chargé avec succès.
    """
    # Charger le modèle au démarrage
    if plate_predictor.load_model():
        logger.info("✅ Modèle chargé avec succès")
    else:
        logger.error("❌ Modèle non chargé")


@app.on_event("shutdown")
async def shutdown_event():
    """
    Libérer les ressources de l'application à l'arrêt.

    Gestionnaire d'événement FastAPI exécuté lors de l'arrêt propre
    du serveur ASGI. Il journalise un message signalant l'arrêt de
    l'API pour faciliter le suivi opérationnel et le diagnostic.
    """
    logger.info("🛑 Arrêt de l'API LRS")


# ==================== Include Routers ====================

# Enregistrement des routeurs FastAPI. Chaque routeur est associé
# à un préfixe d'URL et à un tag OpenAPI pour organiser la
# documentation générée automatiquement.

# Routeur d'authentification : inscription, connexion, gestion des
# tokens JWT.
app.include_router(auth.router, prefix="/auth", tags=["Authentication"])

# Routeur de prédictions : soumission d'images et consultation des
# résultats de reconnaissance de plaques.
app.include_router(predictions.router, prefix="/predictions", tags=["Predictions"])

# Routeur d'administration : gestion des utilisateurs et consultation
# des statistiques globales de la plateforme.
app.include_router(admin.router, prefix="/admin", tags=["Admin"])

# Routeur du modèle : informations sur le pipeline de reconnaissance
# de plaques (nom, version, algorithme, fonctionnalités).
app.include_router(model.router, prefix="/model", tags=["Model"])


@app.get("/", include_in_schema=False)
async def root():
    """
    Endpoint racine de l'API.

    Retourne un message d'accueil avec les informations de base de
    l'API (message d'authentification requise, lien vers la
    documentation OpenAPI, version). Cet endpoint est exclu de la
    documentation OpenAPI générée (``include_in_schema=False``) car
    il sert uniquement de point d'entrée informatif.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``message`` (str) : Message indiquant que
              l'authentification est requise pour accéder à l'API.
            - ``documentation`` (str) : Chemin vers la documentation
              interactive Swagger UI (``/docs``).
            - ``version`` (str) : Version sémantique de l'API.
    """
    return {
        "message": "API LRS - Authentification requise",
        "documentation": "/docs",
        "version": "1.0.0"
    }


# Point d'entrée pour l'exécution autonome de l'application via
# ``python -m app.main``. Lance le serveur Uvicorn sur le port 8000
# avec rechargement automatique activé (mode développement).
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True, log_level="info")
