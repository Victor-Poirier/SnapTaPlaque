"""
auth.py — Routeur d'authentification pour l'API SnapTaPlaque.

Ce module regroupe les endpoints liés à la gestion de l'identité des
utilisateurs : inscription, connexion et consultation du profil courant.

Endpoints exposés :
    - ``POST /login``    — Authentification par identifiant et mot de passe.
      Retourne un token JWT (Bearer) à inclure dans l'en-tête
      ``Authorization`` des requêtes ultérieures.
    - ``POST /register`` — Création d'un nouveau compte utilisateur.
      Le mot de passe est haché côté serveur avant stockage.
    - ``GET /me``        — Récupération des informations de l'utilisateur
      actuellement authentifié (dérivées du token JWT).

Ces endpoints sont montés sous le préfixe ``/auth`` par le routeur
principal de l'application (voir ``app/main.py``).

Dépendances :
    - ``app.database.get_db`` — Fournit une session SQLAlchemy liée à la
      base PostgreSQL, automatiquement fermée en fin de requête.
    - ``app.crud`` — Fonctions CRUD pour la lecture et l'écriture des
      utilisateurs en base de données (``get_user_by_username``,
      ``get_user_by_email``, ``create_user``).
    - ``app.schemas`` — Schémas Pydantic de validation et de sérialisation
      (``Token``, ``UserCreate``, ``UserResponse``).
    - ``app.auth.create_access_token`` — Génère un token JWT signé.
    - ``app.auth.verify_password`` — Compare un mot de passe en clair
      avec son hash bcrypt.
    - ``app.auth.get_current_active_user`` — Extrait et valide l'utilisateur
      courant à partir du token JWT présent dans la requête.

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from fastapi.security import OAuth2PasswordRequestForm

from app.database import get_db
from app import crud, schemas
from app.auth import (
    create_access_token,
    verify_password,
    get_current_active_user,
)

# Instance du routeur FastAPI pour les endpoints d'authentification.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/auth" et le tag "Auth" pour la documentation OpenAPI.
router = APIRouter()

# ================== LOGIN ==================


@router.post("/login", response_model=schemas.Token)
def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db),
):
    """
    Authentifier un utilisateur et retourner un token JWT.

    Accepte des données de formulaire compatibles OAuth2
    (``application/x-www-form-urlencoded``) contenant les champs
    ``username`` et ``password``. Vérifie les identifiants en base de
    données puis génère un token JWT signé en cas de succès.

    Le token retourné doit être inclus dans les requêtes suivantes
    via l'en-tête HTTP::

        Authorization: Bearer <access_token>

    Args:
        form_data (OAuth2PasswordRequestForm): Formulaire OAuth2 contenant
            les champs ``username`` et ``password``, injecté automatiquement
            par FastAPI.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        dict: Dictionnaire conforme au schéma ``Token`` contenant
            ``access_token`` (chaîne JWT) et ``token_type`` (``"bearer"``).

    Raises:
        HTTPException (401): Si le nom d'utilisateur n'existe pas ou si
            le mot de passe fourni ne correspond pas au hash stocké.
    """
    user = crud.get_user_by_username(db, form_data.username)

    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Identifiants incorrects",
        )

    access_token = create_access_token(
        data={"sub": user.username}
    )

    return {
        "access_token": access_token,
        "token_type": "bearer"
    }

# ================== register ==================


@router.post("/register", response_model=schemas.UserResponse)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    """
    Inscrire un nouvel utilisateur sur la plateforme.

    Crée un nouveau compte utilisateur à partir des informations fournies
    dans le corps de la requête (JSON). Le mot de passe est transmis en
    clair via HTTPS et haché côté serveur avec bcrypt avant d'être
    persisté en base de données.

    L'unicité de l'adresse email est vérifiée avant la création : si un
    compte existe déjà avec le même email, une erreur HTTP 400 est levée.

    Args:
        user (schemas.UserCreate): Schéma Pydantic contenant les champs
            ``username``, ``email``, ``password`` et ``full_name``.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        schemas.UserResponse: Représentation publique de l'utilisateur
            nouvellement créé (id, username, email, is_active, is_admin).

    Raises:
        HTTPException (400): Si l'adresse email est déjà associée à un
            compte existant.
    """
    db_user = crud.get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email déjà enregistré")
    return crud.create_user(db=db, user=user)

# ================== ME ==================


@router.get("/me", response_model=schemas.UserResponse)
def read_me(
    current_user=Depends(get_current_active_user),
):
    """
    Récupérer les informations de l'utilisateur actuellement connecté.

    Extrait l'identité de l'utilisateur à partir du token JWT présent
    dans l'en-tête ``Authorization`` de la requête. Retourne les
    informations publiques du compte (mot de passe haché exclu).

    Cet endpoint est utile pour les applications front-end qui ont besoin
    d'afficher le profil de l'utilisateur connecté sans disposer de son
    identifiant au préalable.

    Args:
        current_user: Instance de l'utilisateur authentifié, injectée par
            la dépendance ``get_current_active_user``. Déclenche une
            erreur HTTP 401 si le token est absent, expiré ou invalide.

    Returns:
        schemas.UserResponse: Informations publiques de l'utilisateur
            connecté (id, username, email, is_active, is_admin).
    """
    return current_user
