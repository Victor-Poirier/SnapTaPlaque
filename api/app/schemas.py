"""
schemas.py — Schémas Pydantic de validation et de sérialisation de l'API SnapTaPlaque.

Ce module définit l'ensemble des schémas Pydantic utilisés pour la
validation des données entrantes (requêtes), la sérialisation des
données sortantes (réponses) et le transport d'informations internes
(tokens JWT). Ces schémas constituent le contrat d'interface de l'API
REST et alimentent la documentation OpenAPI générée automatiquement.

Catégories de schémas :
    - **Authentification** — Schémas de gestion de la connexion et des
      tokens JWT (``LoginRequest``, ``Token``).
    - **Prédictions**      — Schémas de validation et de représentation
      des résultats de détection de plaques d'immatriculation
      (``DetectionResult``, ``PredictionResponse``, ``PlateStats``,
      ``PlateHistory``).
    - **Utilisateur**      — Schémas de création et de représentation
      des utilisateurs (``UserCreate``, ``UserResponse``).

Version : 1.0.0
"""

from pydantic import BaseModel, EmailStr, Field
from typing import Optional, Dict, Any, List
from datetime import datetime


# ---------- AUTH ----------

class LoginRequest(BaseModel):
    """
    Schéma de validation pour une requête de connexion.

    Valide les identifiants soumis par l'utilisateur lors de la
    tentative d'authentification. Le nom d'utilisateur et le mot de
    passe sont transmis en clair dans le corps de la requête et
    vérifiés côté serveur via comparaison avec le hash bcrypt stocké
    en base de données.

    Attributes:
        username (str): Nom d'utilisateur unique identifiant le compte
            à authentifier.
        password (str): Mot de passe en clair de l'utilisateur, qui
            sera comparé au hash bcrypt stocké en base de données.
    """

    username: str
    password: str


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


# ================= PREDICTIONS =================

class DetectionResult(BaseModel):
    """
    Schéma de représentation d'un résultat unitaire de détection de plaque.

    Représente les informations extraites par le pipeline de
    reconnaissance de plaques d'immatriculation (détection YOLO + OCR)
    pour une plaque unique détectée dans une image soumise.

    Attributes:
        plate_text (str | None): Texte de la plaque d'immatriculation
            reconnu par le moteur OCR. ``None`` si aucune plaque n'a
            été détectée ou si la lecture OCR a échoué.
        confidence (float | None): Score de confiance de la détection,
            compris entre 0.0 et 1.0. ``None`` si aucune plaque n'a
            été détectée.
        bounding_box (dict | None): Dictionnaire contenant les
            coordonnées de la boîte englobante de la plaque détectée
            dans l'image. ``None`` si les coordonnées ne sont pas
            disponibles.
    """

    plate_text: Optional[str] = None
    confidence: Optional[float] = None
    bounding_box: Optional[dict] = None


class PredictionResponse(BaseModel):
    """
    Schéma de sérialisation pour la représentation d'une prédiction.

    Utilisé pour les réponses API retournant les informations complètes
    d'une prédiction de reconnaissance de plaque, incluant les
    métadonnées (identifiant, utilisateur, fichier, date) et la liste
    des résultats de détection associés.

    Attributes:
        id (int): Identifiant unique auto-incrémenté de la prédiction
            (clé primaire).
        user_id (int): Identifiant de l'utilisateur ayant soumis la
            prédiction (clé étrangère vers ``users.id``).
        filename (str): Nom du fichier image soumis pour la détection
            de plaque d'immatriculation.
        results (list[DetectionResult]): Liste des résultats de
            détection de plaques contenus dans l'image. Chaque élément
            représente une plaque détectée avec son texte, son score
            de confiance et ses coordonnées.
        created_at (datetime): Date et heure de création de la
            prédiction. Définie automatiquement à l'instant de
            l'insertion en base de données.

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``Prediction`` (anciennement ``orm_mode``).
    """

    id: int
    user_id: int
    filename: str
    results: List[DetectionResult]
    created_at: datetime

    class Config:
        from_attributes = True


class PlateStats(BaseModel):
    """
    Statistiques de prédiction d'un utilisateur.

    Schéma utilisé pour représenter les statistiques agrégées de
    détection de plaques d'un utilisateur, incluant le nombre total
    de prédictions effectuées.

    Attributes:
        total_predictions (int): Nombre total de prédictions de
            reconnaissance de plaques effectuées par l'utilisateur.
    """

    total_predictions: int


class PlateHistory(BaseModel):
    """
    Schéma de sérialisation pour l'historique simplifié des prédictions.

    Représente une entrée dans l'historique des prédictions d'un
    utilisateur, sous une forme allégée ne contenant que les
    informations essentielles (identifiant, texte de la plaque,
    confiance, date). Ce schéma est utilisé pour les endpoints de
    consultation d'historique paginé.

    Attributes:
        id (int): Identifiant unique auto-incrémenté de la prédiction
            (clé primaire).
        plate_text (str | None): Texte de la plaque d'immatriculation
            reconnu. ``None`` si aucune plaque n'a été détectée ou si
            la lecture OCR a échoué.
        confidence (float | None): Score de confiance de la détection,
            compris entre 0.0 et 1.0. ``None`` si aucune plaque n'a
            été détectée.
        created_at (datetime): Date et heure de création de la
            prédiction.

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``Prediction`` (anciennement ``orm_mode``).
    """

    id: int
    plate_text: Optional[str] = None
    confidence: Optional[float] = None
    created_at: datetime

    class Config:
        from_attributes = True


# ---------- USER ----------

class UserCreate(BaseModel):
    """
    Schéma de validation pour la création d'un nouvel utilisateur.

    Valide les données soumises lors de l'inscription d'un utilisateur.
    L'adresse email est automatiquement validée par le type ``EmailStr``
    de Pydantic. Le mot de passe est transmis en clair et sera haché
    via bcrypt avant d'être stocké en base de données.

    Attributes:
        email (EmailStr): Adresse email unique de l'utilisateur, validée
            automatiquement par le type ``EmailStr`` de Pydantic.
        username (str): Nom d'utilisateur unique identifiant le compte
            sur la plateforme.
        password (str): Mot de passe en clair de l'utilisateur. Sera
            haché via bcrypt avant d'être persisté en base de données.
        full_name (str): Nom complet de l'utilisateur (prénom et nom).
        is_admin (bool): Indique si l'utilisateur doit être créé avec
            les privilèges d'administration. Par défaut ``False``.
    """

    email: EmailStr
    username: str
    password: str
    full_name: str
    is_admin: bool = False


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

    class Config:
        from_attributes = True