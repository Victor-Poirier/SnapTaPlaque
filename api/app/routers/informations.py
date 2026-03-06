"""
Module des endpoints informationnels non versionnés de l'API SnapTaPlaque.

Ce module définit les endpoints transversaux qui ne dépendent d'aucune
version spécifique du contrat d'API. Ils sont montés directement à la
racine de l'application FastAPI dans main.py, sans préfixe de version
(/v1/, /v2/, etc.).

Ce module est référencé dans main.py via :
    app.include_router(informations.router)

Aucun middleware d'authentification n'est appliqué sur ces endpoints :
ils sont tous accessibles publiquement sans token JWT.

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, status
from app.config import settings

router = APIRouter()


# ==================== Endpoints non versionnés ====================

# Les endpoints ci-dessous ne sont pas versionnés car ils sont
# transversaux à l'ensemble de l'API et ne dépendent pas d'une
# version spécifique du contrat d'API.

@router.get("/", include_in_schema=False)
async def root():
    """
    Endpoint racine de l'API.

    Retourne un message d'accueil avec les informations de base de
    l'API (message d'authentification requise, lien vers la
    documentation OpenAPI, version). Cet endpoint est exclu de la
    documentation OpenAPI générée (``include_in_schema=False``) car
    il sert uniquement de point d'entrée informatif.

    Cet endpoint est utile pour :
        - Vérifier rapidement que le serveur est en ligne et répond
          correctement (health check basique).
        - Fournir aux développeurs un lien direct vers la documentation
          interactive Swagger UI (/docs).
        - Indiquer la version courante de l'API sans avoir à appeler
          l'endpoint /versions.

    Aucune authentification n'est requise pour accéder à cet endpoint.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``message`` (str) : Message indiquant que
              l'authentification est requise pour accéder à l'API.
            - ``documentation`` (str) : Chemin vers la documentation
              interactive Swagger UI (``/docs``).
            - ``version`` (str) : Version sémantique de l'API, lue
              depuis la configuration centralisée (settings.API_VERSION).
    """
    return {
        "message": "API LRS - Authentification requise",
        "documentation": "/docs",
        "version": settings.API_VERSION,
    }


# ==================== Information Version ====================

@router.get("/versions")
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
          en fonction de la version cible (ex. changement de schéma
          de réponse entre /v1/ et /v2/).
        - Les outils de monitoring qui vérifient la disponibilité de
          chaque version déployée et alertent en cas de retrait.
        - La communication aux consommateurs lors d'une phase de
          dépréciation (transition de /v1/ vers /v2/).

    Le schéma de versioning adopté est le versioning par préfixe d'URL
    (``/v1/``, ``/v2/``, etc.), où chaque version majeure peut introduire
    des changements non rétrocompatibles dans les schémas de requête ou
    de réponse. Les versions dépréciées restent disponibles pendant une
    période de transition minimale de 6 mois avant retrait définitif.

    Aucune authentification n'est requise pour accéder à cet endpoint.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``versions`` (list[dict]) : Liste de dictionnaires, chacun
              décrivant une version avec les clés :
                - ``version`` (str) : Identifiant de la version (ex. "v1").
                - ``status`` (str) : Statut de la version parmi
                  "stable", "beta" ou "deprecated".
                - ``pipeline`` (str) : Description du pipeline IA
                  utilisé par cette version (ex. "YOLOv8 + EasyOCR").
            - ``latest`` (str) : Version sémantique de la dernière
              version stable, lue depuis settings.API_VERSION.
    """
    return {
        "versions": [
            {
                "version": "v1",
                "status": "stable",
                "pipeline": "YOLOv8 + EasyOCR",
            }
        ],
        "latest": settings.API_VERSION,
    }


# ==================== RGPD — Politique de confidentialité ====================

@router.get("/privacy-policy")
async def privacy_policy():
    """
    Politique de confidentialité RGPD de l'API SnapTaPlaque.

    Endpoint public (aucune authentification requise) exposant les
    informations de transparence exigées par les articles 13 et 14
    du Règlement Général sur la Protection des Données (RGPD —
    Règlement UE 2016/679).

    Cet endpoint centralise les informations suivantes :
        - Identité et coordonnées du responsable de traitement.
        - Finalité du traitement des données personnelles.
        - Base légale du traitement (Art. 6 RGPD) : consentement
          explicite de l'utilisateur recueilli à l'inscription.
        - Catégories de données personnelles collectées (email,
          nom d'utilisateur, images soumises, résultats de détection).
        - Durée de conservation des données.
        - Droits des utilisateurs et endpoints permettant de les
          exercer (accès, effacement, rectification).
        - Information sur le partage des données avec des tiers.
        - Mesures de sécurité techniques mises en place (Art. 32).

    Les endpoints RGPD associés permettant aux utilisateurs d'exercer
    leurs droits sont :
        - ``GET /v1/auth/me/data-export`` — Droit d'accès (Art. 15)
          et droit à la portabilité (Art. 20). Retourne un export
          JSON structuré de toutes les données personnelles.
        - ``DELETE /v1/auth/me/delete-account`` — Droit à l'effacement
          / droit à l'oubli (Art. 17). Suppression irréversible et
          atomique de toutes les données (favoris, prédictions, profil).

    Les images soumises pour détection ne sont pas conservées après
    traitement, conformément au principe de minimisation des données
    (Art. 5.1.c RGPD) et à la protection des données dès la
    conception (Art. 25 RGPD).

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``controller`` (str) : Nom du responsable de traitement.
            - ``contact`` (str) : Adresse email de contact pour les
              questions relatives à la protection des données.
            - ``purpose`` (str) : Finalité du traitement.
            - ``legal_basis`` (str) : Base légale du traitement
              conformément à l'article 6 du RGPD.
            - ``data_collected`` (list[str]) : Liste des catégories
              de données personnelles collectées par la plateforme.
            - ``retention_period`` (str) : Politique de durée de
              conservation des données personnelles.
            - ``user_rights`` (dict) : Dictionnaire décrivant les
              droits des utilisateurs avec les clés :
                - ``access`` (str) : Endpoint pour le droit d'accès.
                - ``erasure`` (str) : Endpoint pour le droit à l'effacement.
                - ``rectification`` (str) : Procédure de rectification.
            - ``data_sharing`` (str) : Information sur les transferts
              de données à des tiers.
            - ``security_measures`` (list[str]) : Liste des mesures
              de sécurité techniques implémentées (Art. 32 RGPD).
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

@router.get("/health")
async def health_check():
    """
    Vérification de l'état de santé de l'API SnapTaPlaque.

    Endpoint public (aucune authentification requise) permettant de
    vérifier que l'API est opérationnelle et répond correctement.
    Utilisé typiquement par les outils de monitoring, les load balancers
    ou les orchestrateurs de conteneurs (Docker, Kubernetes) pour
    déterminer si l'instance est prête à recevoir du trafic.

    Cet endpoint est volontairement léger et ne vérifie pas les
    dépendances externes (base de données, services tiers) afin
    de garantir un temps de réponse minimal. Pour un diagnostic
    plus complet incluant l'état des dépendances, un endpoint
    /health/ready pourrait être ajouté ultérieurement.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``status`` (str) : État de l'API, valeur "healthy"
              si l'instance répond correctement.
            - ``version`` (str) : Version sémantique de l'API, lue
              depuis la configuration centralisée (settings.API_VERSION).
    """
    return {
        "status": "healthy",
        "version": settings.API_VERSION,
    }