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
    - ``get_vehicle_by_license_plate`` — Recherche les informations d'un
      vehicule par sa plaque d'immatriculation.

Version : 1.0.0
"""

from sqlalchemy.orm import Session
from sqlalchemy import func
from app.database import User, Prediction, Vehicle, user_favorites
from app.schemas import UserCreate
from app.security import get_password_hash

from datetime import datetime


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


def create_user(db: Session, user: UserCreate) -> User:
    """
    Créer un nouvel utilisateur en base de données avec traçabilité du consentement RGPD.

    Hache le mot de passe en clair fourni par l'utilisateur via bcrypt,
    instancie un objet ORM ``User`` avec les informations du schéma
    ``UserCreate``, puis persiste l'enregistrement en base de données.

    Conformité RGPD (Art. 6.1.a & Art. 7) :
        Si l'utilisateur a coché la case de consentement lors de
        l'inscription (``user.gdpr_consent == True``), la date et
        l'heure UTC du consentement sont enregistrées dans le champ
        ``gdpr_consent_at``. Ce champ constitue la preuve horodatée
        du consentement explicite exigée par le RGPD. Si le
        consentement n'est pas donné, ``gdpr_consent_at`` est défini
        à ``None`` (bien que l'endpoint ``/register`` rejette la
        requête en amont dans ce cas).

    Args:
        db (Session): Session SQLAlchemy active, injectée par la
            dépendance ``get_db``.
        user (UserCreate): Schéma Pydantic contenant les données
            d'inscription validées :
            - ``email`` (str) : Adresse email unique.
            - ``username`` (str) : Nom d'utilisateur unique.
            - ``password`` (str) : Mot de passe en clair (haché avant
              stockage, jamais persisté en clair).
            - ``full_name`` (str) : Nom complet de l'utilisateur.
            - ``is_admin`` (bool) : Statut administrateur.
            - ``gdpr_consent`` (bool) : Consentement RGPD explicite.

    Returns:
        User: Instance ORM de l'utilisateur nouvellement créé, avec
            tous les champs rafraîchis depuis la base de données
            (y compris ``id`` auto-incrémenté et ``created_at``).
    """
    # Hachage du mot de passe en clair via bcrypt avant stockage.
    # Le mot de passe en clair n'est jamais persisté en base de données
    # (conformité RGPD Art. 32 — sécurité du traitement).
    hashed_password = get_password_hash(user.password)

    # Construction de l'instance ORM User. Le champ gdpr_consent_at
    # horodate le consentement RGPD à l'instant UTC de l'inscription.
    db_user = User(
        email=user.email,
        username=user.username,
        hashed_password=hashed_password,
        full_name=user.full_name,
        is_admin=user.is_admin,
        gdpr_consent_at=datetime.utcnow() if user.gdpr_consent else None,
    )

    # Persistance en base : ajout, validation de la transaction et
    # rafraîchissement de l'instance pour récupérer les valeurs
    # générées côté serveur (id, created_at).
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user



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

def add_favorite(db: Session, user_id: int, license_plate: str):
    """
    Ajouter un véhicule aux favoris d'un utilisateur.

    Insère une nouvelle entrée dans la table d'association
    ``user_favorites`` liant l'identifiant de l'utilisateur à la plaque
    d'immatriculation du véhicule. Si l'association existe déjà, une
    exception d'intégrité sera levée par la base de données (contrainte
    d'unicité sur le couple ``user_id`` / ``license_plate``).

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur souhaitant ajouter
            le véhicule à ses favoris.
        license_plate (str): Plaque d'immatriculation du véhicule à
            ajouter aux favoris.Raises:
        IntegrityError: Si le véhicule est déjà présent dans la liste
            des favoris de l'utilisateur (doublon détecté par la base).
    """
    stmt = user_favorites.insert().values(user_id=user_id, license_plate=license_plate)
    db.execute(stmt)
    db.commit()


def remove_favorite(db: Session, user_id: int, license_plate: str):
    """
    Retirer un véhicule des favoris d'un utilisateur.

    Supprime l'entrée correspondant au couple ``user_id`` /
    ``license_plate`` dans la table d'association ``user_favorites``.
    Si l'association n'existe pas, l'opération est silencieusement
    ignorée (aucune ligne supprimée, pas d'erreur levée).

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur souhaitant retirer
            le véhicule de ses favoris.
        license_plate (str): Plaque d'immatriculation du véhicule à
            retirer des favoris.
    """
    stmt = user_favorites.delete().where(
        (user_favorites.c.user_id == user_id) &
        (user_favorites.c.license_plate == license_plate)
    )
    db.execute(stmt)
    db.commit()


def get_user_favorites(db: Session, user_id: int):
    """
    Récupérer la liste des véhicules favoris d'un utilisateur.

    Charge l'utilisateur depuis la base de données par son identifiant,
    puis accède à la relation ``favorites`` définie sur le modèle
    ``User``. Cette relation exploite la table d'association
    ``user_favorites`` pour résoudre les véhicules liés.

    Args:
        db (Session): Session SQLAlchemy active.
        user_id (int): Identifiant de l'utilisateur dont on souhaite
            récupérer les véhicules favoris.

    Returns:
        list[Vehicle]: Liste des instances ORM ``Vehicle`` présentes
            dans les favoris de l'utilisateur. Retourne une liste vide
            si l'utilisateur n'existe pas ou s'il n'a aucun favori.
    """
    user = db.query(User).filter(User.id == user_id).first()
    return user.favorites if user else []


def get_vehicle_by_license_plate(db: Session, license_plate: str) :
    """
    Rechercher les informations d'un véhicule par sa plaque d'immatriculation.

    Effectue une requête sur la table ``vehicles`` pour trouver le premier
    enregistrement dont la plaque d'immatriculation correspond exactement à
    la valeur fournie.

    Args:
        db (Session): Session SQLAlchemy active.
        license_plate (str): Plaque d'immatriculation à rechercher.

    Returns:
        Instance ORM de ``Vehicle`` correspondant à la plaque d'immatriculation fournie,
        ou ``None`` si aucun véhicule ne correspond.
    """
    return db.query(Vehicle).filter(Vehicle.license_plate == license_plate).first()

def create_vehicle(db: Session, vehicle_data: dict) -> Vehicle:
    """
    Créer un nouvel enregistrement de véhicule en base de données.

    Args:
        db (Session): Session SQLAlchemy active.
        vehicle_data (dict): Dictionnaire contenant les données du véhicule
            à créer. Doit inclure au minimum la clé ``license_plate``.

    Returns:
        Vehicle: Instance ORM du véhicule nouvellement créé, avec tous les
            champs rafraîchis depuis la base de données (y compris ``id``).
    """
    db_vehicle = Vehicle(**vehicle_data)
    db.add(db_vehicle)
    db.commit()
    db.refresh(db_vehicle)
    print(f"Véhicule créé en base de données : {db_vehicle}")
    return db_vehicle

def get_vehicle_info_history_by_user(db: Session, user_id: int):
    # Récupérer l'historique des informations de véhicules consultés ou enregistrés par un utilisateur. L'historique 
    # ce situe dans la table User et est lié à la table Vehicle via une relation one-to-many. 
    user = db.query(User).filter(User.id == user_id).first()
    return user.vehicles_info_history if user else []

def create_vehicle_info_history(db: Session, user_id: int, license_plate: str):
    # Ajouter une entrée dans l'historique des informations de véhicules consultés ou enregistrés par un utilisateur.
    # Cette fonction crée un nouvel enregistrement dans la table VehicleInfoHistory, associant l'utilisateur, la plaque 
    # d'immatriculation du véhicule et les informations correspondantes. 
    vehicle = get_vehicle_by_license_plate(db, license_plate)
    user = db.query(User).filter(User.id == user_id).first()
    user.vehicles_info_history.append(vehicle)
    db.commit()

    