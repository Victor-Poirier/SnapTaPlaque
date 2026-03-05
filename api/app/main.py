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
    - ``list_versions``    — Endpoint de découverte des versions
      (``GET /versions``) retournant la liste des versions disponibles
      de l'API avec leur statut et le pipeline IA associé.
    - ``privacy_policy``   — Endpoint RGPD (``GET /privacy-policy``)
      exposant la politique de confidentialité conforme aux articles
      13 et 14 du RGPD (responsable de traitement, finalité, base
      légale, droits des utilisateurs, mesures de sécurité).

Routeurs enregistrés :
    - ``/v1/account``         — Endpoints d'authentification (inscription,
      connexion, consultation du profil via token JWT, export des
      données personnelles RGPD, suppression de compte RGPD).
    - ``/v1/predictions``  — Endpoints de soumission d'images et de
      consultation des prédictions de reconnaissance de plaques
      (historique, statistiques).
    - ``/v1/admin``        — Endpoints d'administration (liste des
      utilisateurs, statistiques globales de la plateforme).
    - ``/v1/model``        — Endpoints d'information sur le pipeline de
      reconnaissance de plaques (nom, version, algorithme).
    - ``/v1/vehicles``     — Endpoints de consultation des informations
      détaillées d'un véhicule à partir de sa plaque d'immatriculation.
    - ``/v1/favorites``    — Endpoints de gestion des véhicules favoris
      de l'utilisateur connecté (ajout, suppression, consultation).

Versioning :
    L'API adopte un schéma de versioning par préfixe d'URL (``/v1/``,
    ``/v2/``, etc.). Chaque version majeure est isolée dans un sous-
    package ``app.routers.v1``, ``app.routers.v2``, etc., permettant
    une évolution indépendante des contrats d'API sans casser la
    rétrocompatibilité pour les consommateurs existants.

    - **v1** (stable) — Version initiale de l'API, utilisant le pipeline
      YOLOv8 + EasyOCR pour la reconnaissance de plaques.
    - **v2** (prévu) — Version future pouvant introduire de nouveaux
      modèles ou des changements de schéma de réponse.

    L'endpoint ``GET /versions`` permet aux clients de découvrir
    dynamiquement les versions disponibles et leur statut.

Conformité RGPD :
    L'API intègre les mécanismes requis par le Règlement Général sur
    la Protection des Données (RGPD — Règlement UE 2016/679) :

    - **Consentement explicite (Art. 6.1.a)** — L'inscription requiert
      l'acceptation explicite de la politique de confidentialité via
      le champ ``gdpr_consent`` du schéma ``UserCreate``. La date du
      consentement est horodatée en base (``gdpr_consent_at``).
    - **Droit d'accès et portabilité (Art. 15 & 20)** — L'endpoint
      ``GET /v1/account/me/data-export`` permet à l'utilisateur
      d'exporter l'intégralité de ses données personnelles au format
      JSON structuré (profil, prédictions, favoris).
    - **Droit à l'effacement (Art. 17)** — L'endpoint
      ``DELETE /v1/account/me/delete-account`` supprime de manière
      irréversible toutes les données de l'utilisateur (favoris,
      prédictions, profil).
    - **Information transparente (Art. 13 & 14)** — L'endpoint
      ``GET /privacy-policy`` expose publiquement la politique de
      confidentialité détaillant le responsable de traitement, la
      finalité, la base légale, les données collectées, la durée de
      conservation, les droits et les mesures de sécurité.

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

# ==================== Dependencies ====================

from fastapi import FastAPI
import warnings
import logging
from app.database import create_tables
from app.predictor import plate_predictor
from app.routers.v1 import account, predictions, admin, model, vehicles, favorites
from app.crud import (
    get_user_by_email, get_user_by_username, create_user, authenticate_user,
    create_prediction, get_user_predictions
)
from slowapi import _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from app.limiter import limiter
from app.routers import informations
from app.config import settings


warnings.filterwarnings("ignore", category=FutureWarning, module="easyocr")
warnings.filterwarnings("ignore", category=FutureWarning, module="torch")

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
    title=settings.API_TITLE,
    version=settings.API_VERSION,
    description=settings.API_DESCRIPTION,
)

# Le limitateur utilise l'adresse IP du client comme
# clé d'identification pour appliquer les limites de requêtes. En cas de
# dépassement, une exception ``RateLimitExceeded`` est levée, qui est gérée
# par le handler personnalisé ``_rate_limit_exceeded_handler`` pour retourner
# une réponse HTTP 429 avec un message d'erreur clair.
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)


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
# à un préfixe d'URL versionné (``/v1/...``) et à un tag OpenAPI
# pour organiser la documentation générée automatiquement.
# Le versioning par préfixe d'URL permet de faire coexister
# plusieurs versions de l'API sur la même instance applicative.

# Routeur d'authentification : inscription, connexion, consultation
# du profil via token JWT, export des données personnelles (RGPD
# Art. 15 & 20) et suppression de compte (RGPD Art. 17).
app.include_router(account.router, prefix="/v1/account", tags=["Account"])

# Routeur de prédictions : soumission d'images, consultation de
# l'historique et des statistiques de reconnaissance de plaques.
app.include_router(predictions.router, prefix="/v1/predictions", tags=["Predictions"])

# Routeur d'administration : liste des utilisateurs et consultation
# des statistiques globales de la plateforme. Réservé aux
# administrateurs.
app.include_router(admin.router, prefix="/v1/admin", tags=["Admin"])

# Routeur du modèle : informations sur le pipeline de reconnaissance
# de plaques (nom, version, algorithme, fonctionnalités).
app.include_router(model.router, prefix="/v1/model", tags=["Model"])

# Routeur des véhicules : consultation des informations détaillées
# d'un véhicule à partir de sa plaque d'immatriculation.
app.include_router(vehicles.router, prefix="/v1/vehicles", tags=["Vehicles Info"])

# Routeur des favoris : ajout, suppression et consultation des
# véhicules favoris de l'utilisateur connecté.
app.include_router(favorites.router, prefix="/v1/favorites", tags=["Favorites"])

# Routers d'informations générales : endpoint racine (``/``) et endpoint de
# découverte des versions (``/versions``) de l'API. Ces endpoints sont exposés sans
# préfixe de version car ils sont transversaux à l'ensemble de l'API et ne dépendent
# pas d'une version spécifique du contrat d'API. Ils servent de point d'entrée informatif
# et de mécanisme de découverte pour les utilisateurs de l'API.
app.include_router(informations.router, prefix="", tags=["Global Informations"])

# Note sur le versioning :
# Exemple avec une V2 du routeur de prédictions, qui pourrait introduire des changements.
#
# app.include_router(predictions_v2.router, prefix="/v2/predictions", tags=["V2 - Predictions"])
# Les autres endpoints V2 réutilisent V1 tant qu'ils ne changent pas
# app.include_router(auth_v1.router, prefix="/v2/account", tags=["V2 - Account"])
# app.include_router(admin_v1.router, prefix="/v2/admin", tags=["V2 - Admin"])
# app.include_router(model_v1.router, prefix="/v2/model", tags=["V2 - Model"])
# app.include_router(vehicles_v1.router, prefix="/v2/vehicles", tags=["V2 - Vehicles"])
# app.include_router(favorites_v1.router, prefix="/v2/favorites", tags=["V2 - Favorites"])

# ==================== Main Entry ====================

# Point d'entrée pour l'exécution autonome de l'application via
# ``python -m app.main``. Lance le serveur Uvicorn sur le port 8000
# avec rechargement automatique activé (mode développement).
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True, log_level="info")
