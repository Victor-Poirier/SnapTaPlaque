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
    - ``/v1/auth``         — Endpoints d'authentification (inscription,
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
      ``GET /v1/auth/me/data-export`` permet à l'utilisateur
      d'exporter l'intégralité de ses données personnelles au format
      JSON structuré (profil, prédictions, favoris).
    - **Droit à l'effacement (Art. 17)** — L'endpoint
      ``DELETE /v1/auth/me/delete-account`` supprime de manière
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

from fastapi import FastAPI
import logging

from app.database import create_tables
from app.predictor import plate_predictor

from app.routers.v1 import auth, predictions, admin, model, vehicles, favorites

from app.crud import (
    get_user_by_email, get_user_by_username, create_user, authenticate_user,
    create_prediction, get_user_predictions
)

from slowapi import _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from app.limiter import limiter

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
app.include_router(auth.router, prefix="/v1/auth", tags=["Authentication"])

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

# Exemple avec une V2 du routeur de prédictions, qui pourrait introduire des changements.
#
# app.include_router(predictions_v2.router, prefix="/v2/predictions", tags=["V2 - Predictions"])
# Les autres endpoints V2 réutilisent V1 tant qu'ils ne changent pas
# app.include_router(auth_v1.router, prefix="/v2/auth", tags=["V2 - Auth"])
# app.include_router(admin_v1.router, prefix="/v2/admin", tags=["V2 - Admin"])
# app.include_router(model_v1.router, prefix="/v2/model", tags=["V2 - Model"])
# app.include_router(vehicles_v1.router, prefix="/v2/vehicles", tags=["V2 - Vehicles"])
# app.include_router(favorites_v1.router, prefix="/v2/favorites", tags=["V2 - Favorites"])


# ==================== Endpoints non versionnés ====================

# Les endpoints ci-dessous ne sont pas versionnés car ils sont
# transversaux à l'ensemble de l'API et ne dépendent pas d'une
# version spécifique du contrat d'API.

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


# ==================== Information Version ====================

@app.get("/versions")
async def list_versions():
    """
    Lister les versions disponibles de l'API SnapTaPlaque.

    Retourne un dictionnaire décrivant toutes les versions de l'API
    actuellement servies par cette instance, accompagnées de leur statut
    (stable, beta, deprecated) et du pipeline de reconnaissance de plaques
    associé. Cet endpoint permet aux clients de découvrir dynamiquement
    les versions disponibles et de choisir celle qui correspond à leurs
    besoins.

    Ce mécanisme de découverte est particulièrement utile pour :
        - Les applications front-end qui doivent adapter leurs appels
          selon la version la plus récente ou la plus stable.
        - Les outils de monitoring qui vérifient la disponibilité de
          chaque version.
        - La communication aux consommateurs lors d'une phase de
          dépréciation ou de migration vers une nouvelle version.

    Le schéma de versioning adopté est le versioning par préfixe d'URL
    (``/v1/``, ``/v2/``, etc.), où chaque version majeure peut introduire
    des changements non rétrocompatibles dans les schémas de requête ou
    de réponse.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``versions`` (list[dict]) : Liste des versions disponibles,
              chacune décrite par :
                - ``version`` (str) : Identifiant de la version (ex. ``"v1"``).
                - ``status`` (str) : Statut courant — ``"stable"``,
                  ``"beta"`` ou ``"deprecated"``.
                - ``model`` (str) : Description du pipeline IA utilisé
                  par cette version (ex. ``"YOLOv8 + EasyOCR"``).
                - ``deprecated`` (bool) : ``True`` si la version est
                  marquée pour retrait futur, ``False`` sinon.
            - ``latest`` (str) : Identifiant de la version recommandée
              (la plus récente en statut stable ou beta).
    """
    return {
        "versions": [
            {
                "version": "v1",
                "status": "stable",
                "model": "YOLOv8 + EasyOCR",
                "deprecated": False,
            },
            # Décommenter lors de l'activation de la V2 :
            # {
            #     "version": "v2",
            #     "status": "beta",
            #     "model": "YOLOv.. + "...",
            #     "deprecated": False,
            # },
        ],
        "latest": "v1",
    }


# ==================== RGPD — Politique de confidentialité ====================

@app.get("/privacy-policy")
async def privacy_policy():
    """
    Politique de confidentialité RGPD de l'API SnapTaPlaque.

    Endpoint public (aucune authentification requise) exposant les
    informations de transparence exigées par les articles 13 et 14
    du Règlement Général sur la Protection des Données (RGPD —
    Règlement UE 2016/679).

    Cet endpoint centralise les informations suivantes :
        - **Responsable de traitement** — Identité et coordonnées
          du responsable du traitement des données personnelles.
        - **Finalité du traitement** — Description claire de l'objectif
          pour lequel les données sont collectées et traitées.
        - **Base légale** — Fondement juridique du traitement
          (consentement explicite, Art. 6.1.a RGPD).
        - **Données collectées** — Liste exhaustive des catégories de
          données personnelles traitées par la plateforme.
        - **Durée de conservation** — Politique de rétention des données.
        - **Droits des utilisateurs** — Moyens concrets d'exercer les
          droits d'accès (Art. 15), de portabilité (Art. 20),
          d'effacement (Art. 17) et de rectification (Art. 16).
        - **Transferts de données** — Information sur le partage
          éventuel avec des tiers.
        - **Mesures de sécurité** — Description des dispositifs
          techniques mis en œuvre pour protéger les données.

    Les endpoints RGPD associés permettant aux utilisateurs d'exercer
    leurs droits sont :
        - ``GET /v1/auth/me/data-export`` — Droit d'accès et portabilité
          (Art. 15 & 20).
        - ``DELETE /v1/auth/me/delete-account`` — Droit à l'effacement
          (Art. 17).

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``controller`` (str) : Nom du responsable du traitement des
              données (ex. ``"Projet universitaire SnapTaPlaque"``).
            - ``contact`` (str) : Adresse email de contact pour les
              questions relatives à la confidentialité.
            - ``purpose`` (str) : Finalité du traitement des données
              personnelles.
            - ``legal_basis`` (str) : Base légale du traitement (ex.
              ``"Consentement explicite de l'utilisateur (Art. 6.1.a RGPD)"``).
            - ``data_collected`` (list[str]) : Liste des catégories de
              données personnelles collectées.
            - ``retention_period`` (str) : Durée de conservation des
              données personnelles.
            - ``user_rights`` (dict) : Description des droits des
              utilisateurs et des endpoints permettant de les exercer.
            - ``data_sharing`` (str) : Information sur le partage des
              données avec des tiers.
            - ``security_measures`` (list[str]) : Liste des mesures de
              sécurité mises en place pour protéger les données personnelles.
    """
    return {
        "controller": "Projet universitaire SnapTaPlaque",
        "contact": "vincent.proudy.etu@univ-lemans.fr",
        "purpose": "Reconnaissance de plaques d'immatriculation à des fins pédagogiques",
        "legal_basis": "Consentement explicite de l'utilisateur (Art. 6.1.a RGPD)",
        "data_collected": [
            "Email, nom d'utilisateur, nom complet (inscription)",
            "Images soumises pour détection (non conservées après traitement)",
            "Résultats de détection (plaques reconnues, scores de confiance)",
            "Adresse IP (rate limiting uniquement, non stockée en base)",
        ],
        "retention_period": "Données conservées jusqu'à suppression du compte par l'utilisateur",
        "user_rights": {
            "access": "GET /v1/auth/me/data-export",
            "erasure": "DELETE /v1/auth/me/delete-account",
            "rectification": "Contacter le responsable de traitement",
        },
        "data_sharing": "Aucun transfert à des tiers",
        "security_measures": [
            "Mots de passe hachés avec bcrypt",
            "Authentification par token JWT",
            "Rate limiting sur les endpoints sensibles",
        ],
    }


# Point d'entrée pour l'exécution autonome de l'application via
# ``python -m app.main``. Lance le serveur Uvicorn sur le port 8000
# avec rechargement automatique activé (mode développement).
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True, log_level="info")
