"""
models.py — Schémas Pydantic de validation et de sérialisation de l'API SnapTaPlaque.

Ce module définit l'ensemble des schémas Pydantic utilisés pour la
validation des données entrantes (requêtes), la sérialisation des
données sortantes (réponses) et le transport d'informations internes
(tokens JWT). Ces schémas constituent le contrat d'interface de l'API
REST et alimentent la documentation OpenAPI générée automatiquement.

Catégories de schémas :
    - **Utilisateur**   — Schémas de création et de représentation des
      utilisateurs (``UserCreate``, ``UserResponse``).
    - **Authentification** — Schémas de gestion des tokens JWT
      (``Token``, ``TokenWithRefresh``, ``TokenData``).

Validation :
    Les schémas utilisent les fonctionnalités avancées de Pydantic v2
    pour la validation des données : contraintes de champs (``Field``),
    validateurs personnalisés (``@field_validator``), types spécialisés
    (``EmailStr``, ``Literal``), et exemples intégrés à la documentation
    OpenAPI (``json_schema_extra``).

Version : 1.0.0
"""

from pydantic import BaseModel, Field, EmailStr, field_validator
from typing import Literal, Optional
from datetime import datetime

# -------------------- SCHÉMAS UTILISATEUR --------------------

class UserResponse(BaseModel):
    """
    Schéma de sérialisation pour la représentation d'un utilisateur.

    Utilisé pour les réponses API retournant les informations d'un
    utilisateur. Ce schéma exclut volontairement le mot de passe haché
    afin de ne jamais exposer cette donnée sensible via l'API.

    Attributes:
        id (int): Identifiant unique auto-incrémenté de l'utilisateur.
        email (str): Adresse email de l'utilisateur.
        username (str): Nom d'utilisateur unique.
        full_name (str | None): Nom complet de l'utilisateur (prénom et
            nom). ``None`` si non renseigné.
        is_active (bool): Indique si le compte de l'utilisateur est
            actif. Un compte désactivé ne peut pas s'authentifier.
        is_admin (bool): Indique si l'utilisateur possède les privilèges
            d'administration.
        created_at (datetime): Date et heure de création du compte
            utilisateur.

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``User`` (anciennement ``orm_mode``).
    """

    id: int
    email: str
    username: str
    full_name: Optional[str]
    is_active: bool
    is_admin: bool
    created_at: datetime

    model_config = {"from_attributes": True}


# -------------------- SCHÉMAS AUTHENTIFICATION --------------------

class Token(BaseModel):
    """
    Schéma de réponse pour un token JWT d'accès.

    Représente le token d'accès retourné à l'utilisateur après une
    authentification réussie. Ce schéma suit la convention OAuth2
    avec les champs ``access_token`` et ``token_type``.

    Attributes:
        access_token (str): Token JWT signé contenant les informations
            d'identification de l'utilisateur (claims), utilisé pour
            authentifier les requêtes ultérieures via le header
            ``Authorization: Bearer <token>``.
        token_type (str): Type du token, toujours ``"bearer"``
            conformément à la spécification OAuth2.
    """

    access_token: str
    token_type: str = "bearer"


class TokenWithRefresh(Token):
    """
    Schéma de réponse pour un token JWT avec refresh token.

    Étend le schéma ``Token`` en ajoutant un refresh token permettant
    à l'utilisateur de renouveler son token d'accès sans se
    ré-authentifier. Le refresh token possède une durée de validité
    plus longue que le token d'accès.

    Attributes:
        access_token (str): Token JWT d'accès (hérité de ``Token``).
        token_type (str): Type du token, ``"bearer"`` (hérité de
            ``Token``).
        refresh_token (str): Token JWT de rafraîchissement, utilisé
            pour obtenir un nouveau token d'accès lorsque celui-ci
            expire.

    Configuration:
        ``json_schema_extra`` — Exemple de réponse intégré à la
        documentation OpenAPI illustrant la structure complète du
        token avec refresh.
    """

    refresh_token: str

    class Config:
        """
        Configuration interne du schéma Pydantic.

        Attributes:
            json_schema_extra (dict): Exemple de réponse intégré à la
                documentation OpenAPI pour faciliter la compréhension
                du format de retour.
        """

        json_schema_extra = {
            "example": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "bearer",
                "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
        }


class TokenData(BaseModel):
    """
    Schéma de transport des données extraites d'un token JWT.

    Représente les informations (claims) décodées à partir d'un token
    JWT après vérification de sa signature et de sa validité. Ce schéma
    est utilisé en interne par les dépendances d'authentification pour
    transmettre l'identité de l'utilisateur aux endpoints protégés.

    Attributes:
        username (str | None): Nom d'utilisateur extrait du claim
            ``sub`` (subject) du token JWT. ``None`` si le claim est
            absent ou invalide.
        token_type (str | None): Type du token extrait des claims
            personnalisés, permettant de distinguer les tokens d'accès
            (``"access"``) des tokens de rafraîchissement
            (``"refresh"``). ``None`` si le type n'est pas spécifié.
    """

    username: Optional[str] = None
    token_type: Optional[str] = None  # "access" ou "refresh"
