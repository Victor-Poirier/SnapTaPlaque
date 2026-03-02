"""
main.py — Point d'entrée de l'application FastAPI SnapTaPlaque.

Ce module constitue le point d'entrée principal de l'API SnapTaPlaque.
Il initialise l'application FastAPI, configure le système de
journalisation, enregistre les routeurs (authentification, prédictions,
administration, modèle, véhicules, favoris), et définit les événements
de cycle de vie (démarrage et arrêt) de l'application.

Composants exposés :
    - ``app``              — Instance principale de l'application
      FastAPI, point d'entrée ASGI pour le serveur Uvicorn.
    - ``startup_event``    — Gestionnaire d'événement exécuté au
      démarrage de l'application, responsable de la création des tables
      en base de données et du chargement du modèle de reconnaissance
      de plaques.
    - ``shutdown_event``   — Gestionnaire d'événement exécuté à l'arrêt
      de l'application, responsable de la journalisation de l'arrêt.
    - ``root``             — Endpoint racine (``GET /``) retournant un
      message d'accueil avec les informations de base de l'API.

Routeurs enregistrés :
    - ``/auth``            — Endpoints d'authentification (inscription,
      connexion, consultation du profil via token JWT).
    - ``/predictions``     — Endpoints de soumission d'images et de
      consultation des prédictions de reconnaissance de plaques
      (historique, statistiques).
    - ``/admin``           — Endpoints d'administration (liste des
      utilisateurs, statistiques globales de la plateforme).
    - ``/model``           — Endpoints d'information sur le modèle de
      reconnaissance de plaques (nom, version, algorithme).
    - ``/vehicles``        — Endpoints de consultation des informations
      détaillées d'un véhicule à partir de sa plaque d'immatriculation.
    - ``/favorites``       — Endpoints de gestion des véhicules favoris
      de l'utilisateur connecté (ajout, suppression, consultation).

Cycle de vie :
    - **Démarrage** — Les tables de la base de données sont créées (si
      elles n'existent pas) via ``create_tables()``, puis le modèle de
      reconnaissance de plaques est chargé en mémoire via
      ``plate_predictor.load_model()``. Un message de succès ou d'erreur
      est journalisé selon le résultat du chargement.
    - **Arrêt** — Un message de journalisation est émis pour signaler
      l'arrêt propre de l'API.

Version : 1.0.0
"""

from fastapi import FastAPI
import logging

from app.database import create_tables
from app.predictor import plate_predictor
from app.routers import auth, predictions, admin, model, vehicles, favorites

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
    démarrage du serveur ASGI. Il procède en deux étapes :

        1. **Création des tables** — Appelle ``create_tables()`` pour
           générer les tables SQL à partir des modèles ORM SQLAlchemy
           si elles n'existent pas encore en base de données (DDL
           auto-généré via ``Base.metadata.create_all``).
        2. **Chargement du modèle** — Charge le modèle de reconnaissance
           de plaques en mémoire via ``plate_predictor.load_model()`` et
           journalise le résultat de l'opération.

    En cas d'échec du chargement du modèle, l'application démarre
    malgré tout mais les endpoints de prédiction retourneront une
    erreur HTTP 503 tant que le modèle n'aura pas été chargé avec
    succès.
    """

    create_tables()

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

# Routeur d'authentification : inscription, connexion, consultation
# du profil via token JWT.
app.include_router(auth.router, prefix="/auth", tags=["Authentication"])

# Routeur de prédictions : soumission d'images, consultation de
# l'historique et des statistiques de reconnaissance de plaques.
app.include_router(predictions.router, prefix="/predictions", tags=["Predictions"])

# Routeur d'administration : liste des utilisateurs et consultation
# des statistiques globales de la plateforme. Réservé aux
# administrateurs.
app.include_router(admin.router, prefix="/admin", tags=["Admin"])

# Routeur du modèle : informations sur le pipeline de reconnaissance
# de plaques (nom, version, algorithme, fonctionnalités).
app.include_router(model.router, prefix="/model", tags=["Model"])

# Routeur des véhicules : consultation des informations détaillées
# d'un véhicule à partir de sa plaque d'immatriculation.
app.include_router(vehicles.router, prefix="/vehicles", tags=["Vehicles Info"])

# Routeur des favoris : ajout, suppression et consultation des
# véhicules favoris de l'utilisateur connecté.
app.include_router(favorites.router, prefix="/favorites", tags=["Favorites"])

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
