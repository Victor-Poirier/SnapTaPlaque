"""
crud.py — Fonctions CRUD pour l'accès aux données de l'API SnapTaPlaque.

Ce module regroupe l'ensemble des opérations de création, lecture et
agrégation effectuées sur les modèles ORM ``User`` et ``Prediction``
via SQLAlchemy. Il constitue la couche d'accès aux données (DAL) de
l'application, appelée par les routeurs FastAPI.

Fonctions exposées :
    - ``get_user_by_email``        — Recherche un utilisateur par adresse
      email.
    - ``get_user_by_username``     — Recherche un utilisateur par nom
      d'utilisateur.
    - ``create_user``              — Crée un nouvel utilisateur en base
      de données avec hachage du mot de passe.
    - ``authenticate_user``        — Vérifie les identifiants d'un
      utilisateur (nom d'utilisateur + mot de passe).
    - ``create_prediction``        — Enregistre une nouvelle prédiction
      associée à un utilisateur.
    - ``get_user_predictions``     — Récupère la liste paginée des
      prédictions d'un utilisateur.
    - ``get_user_prediction_stats``— Retourne les statistiques de
      prédiction d'un utilisateur (nombre total).
    - ``get_all_users``            — Récupère la liste complète des
      utilisateurs enregistrés.
    - ``get_global_stats``         — Retourne les statistiques globales
      de la plateforme (nombre d'utilisateurs et de prédictions).

Version : 1.0.0
"""

from sqlalchemy.orm import Session
from sqlalchemy import func
from app.database import User, Prediction
from app.models import UserCreate
from sqlalchemy.orm import Session

from app import models
from app.security import get_password_hash

from typing import Optional


def get_user_by_email(db: Session, email: str) -> User:
    """
    Rechercher un utilisateur par son adresse email.

    Effectue une requête sur la table ``users`` pour trouver le premier
    enregistrement dont l'adresse email correspond exactement à la
    valeur fournie.

    Args:
        db (Session): Session SQLAlchemy active.
        email (str): Adresse email à rechercher.

    Returns:
        User: Instance ORM de l'utilisateur trouvé, ou ``None`` si
            aucun utilisateur ne correspond à l'email fourni.
    """
    return db.query(User).filter(User.email == email).first()


def get_user_by_username(db: Session, username: str) -> User:
    """
    Rechercher un utilisateur par son nom d'utilisateur.

    Effectue une requête sur la table ``users`` pour trouver le premier
    enregistrement dont le nom d'utilisateur correspond exactement à la
    valeur fournie.

    Args:
        db (Session): Session SQLAlchemy active.
        username (str): Nom d'utilisateur à rechercher.

    Returns:
        User: Instance ORM de l'utilisateur trouvé, ou ``None`` si
            aucun utilisateur ne correspond au nom fourni.
    """
    return db.query(User).filter(User.username == username).first()


def create_user(
        db: Session,
        user: UserCreate,
):
    """
    Créer un nouvel utilisateur en base de données.

    Hache le mot de passe en clair fourni dans le schéma ``UserCreate``
    via bcrypt, puis crée et persiste un nouvel enregistrement dans la
    table ``users``. L'instance ORM est rafraîchie après le commit pour
    inclure les champs générés par la base (``id``, ``created_at``, etc.).

    Args:
        db (Session): Session SQLAlchemy active.
        user (UserCreate): Schéma Pydantic contenant les champs
            ``email``, ``username``, ``password``, ``full_name`` et
            ``is_admin``.

    Returns:
        User: Instance ORM de l'utilisateur nouvellement créé, incluant
            son identifiant généré par la base de données.
    """
    hashed_password = get_password_hash(user.password)

    db_user = User(
        email=user.email,
        username=user.username,
        hashed_password=hashed_password,
        full_name=user.full_name,
        is_admin=user.is_admin
    )

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def authenticate_user(db: Session, username: str, password: str):
    """
    Authentifier un utilisateur par nom d'utilisateur et mot de passe.

    Recherche l'utilisateur en base de données par son nom d'utilisateur,
    puis vérifie que le mot de passe en clair fourni correspond au hash
    bcrypt stocké. L'import de ``verify_password`` est effectué localement
    pour éviter les dépendances circulaires avec le module ``app.auth``.

    Args:
        db (Session): Session SQLAlchemy active.
        username (str): Nom d'utilisateur à authentifier.
        password (str): Mot de passe en clair soumis par l'utilisateur.

    Returns:
        User: Instance ORM de l'utilisateur authentifié si les
            identifiants sont valides, ``None`` sinon.
    """
    user = get_user_by_username(db, username)
    if not user:
        return None
    from app.auth import verify_password
    if not verify_password(password, user.hashed_password):
        return None
    return user


#######################################################################
# A Modifier en dessous

def create_prediction(
        db: Session,
        user_id: int,
        filename: str,
        results: dict,
) -> Prediction:
    """
    Enregistrer une nouvelle prédiction en base de données.

    Crée un enregistrement dans la table ``predictions`` associant les
    résultats d'une reconnaissance de plaque à l'utilisateur ayant
    soumis l'image. L'instance ORM est rafraîchie après le commit pour
    inclure les champs générés par la base (``id``, ``created_at``, etc.).

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur propriétaire de
            la prédiction.
        filename (str): Nom du fichier image soumis pour la détection.
        results (dict): Dictionnaire contenant les résultats de la
            reconnaissance (plaques détectées, scores de confiance,
            coordonnées des boîtes englobantes, etc.).

    Returns:
        Prediction: Instance ORM de la prédiction nouvellement créée,
            incluant son identifiant généré par la base de données.
    """
    db_prediction = Prediction(
        user_id=user_id,
        filename=filename,
        results=results,
    )
    db.add(db_prediction)
    db.commit()
    db.refresh(db_prediction)
    return db_prediction


def get_user_predictions(db: Session, user_id: int, skip: int = 0, limit: int = 100):
    """
    Récupérer la liste paginée des prédictions d'un utilisateur.

    Effectue une requête sur la table ``predictions`` filtrée par
    l'identifiant de l'utilisateur, avec support de la pagination via
    les paramètres ``skip`` (offset) et ``limit``.

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur dont on souhaite
            récupérer les prédictions.
        skip (int): Nombre d'enregistrements à ignorer en début de
            résultat (offset). Par défaut ``0``.
        limit (int): Nombre maximal d'enregistrements à retourner.
            Par défaut ``100``.

    Returns:
        list[Prediction]: Liste des instances ORM ``Prediction``
            correspondant aux critères de recherche.
    """
    return db.query(Prediction).filter(Prediction.user_id == user_id).offset(skip).limit(limit).all()


def get_user_prediction_stats(db: Session, user_id: int):
    """
    Calculer les statistiques de prédiction d'un utilisateur.

    Compte le nombre total de prédictions enregistrées en base de
    données pour l'utilisateur spécifié.

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur dont on souhaite
            obtenir les statistiques.

    Returns:
        dict: Dictionnaire contenant la clé ``total_predictions`` (int)
            indiquant le nombre total de prédictions de l'utilisateur.
    """
    total = db.query(Prediction).filter(Prediction.user_id == user_id).count()
    return {"total_predictions": total}


#######################################################################

def get_all_users(db: Session):
    """
    Récupérer la liste complète des utilisateurs enregistrés.

    Effectue une requête sans filtre sur la table ``users`` et retourne
    l'ensemble des enregistrements. Cette fonction est principalement
    destinée aux endpoints d'administration.

    Args:
        db (Session): Session SQLAlchemy active.

    Returns:
        list[User]: Liste de toutes les instances ORM ``User``
            présentes en base de données.
    """
    return db.query(User).all()


def get_global_stats(db: Session) -> dict:
    """
    Calculer les statistiques globales de la plateforme.

    Agrège le nombre total d'utilisateurs et de prédictions enregistrés
    en base de données à l'aide de la fonction SQL ``COUNT``. Cette
    fonction est principalement destinée aux tableaux de bord
    d'administration et de supervision.

    Args:
        db (Session): Session SQLAlchemy active.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``total_users`` (int) : Nombre total d'utilisateurs
              enregistrés sur la plateforme.
            - ``total_predictions`` (int) : Nombre total de prédictions
              effectuées sur la plateforme.
    """
    total_users = db.query(func.count(User.id)).scalar()
    total_predictions = db.query(func.count(Prediction.id)).scalar()

    return {
        "total_users": total_users,
        "total_predictions": total_predictions,
    }