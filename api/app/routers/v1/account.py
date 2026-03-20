"""
account.py — Routeur d'authentification pour l'API SnapTaPlaque.

Ce module regroupe les endpoints liés à la gestion de l'identité des
utilisateurs : inscription, connexion, consultation du profil courant,
ainsi que les endpoints de conformité RGPD (export des données
personnelles et suppression de compte).

Endpoints exposés :
    - ``POST /register``        — Création d'un nouveau compte utilisateur.
      Le consentement RGPD (Art. 6.1.a) est vérifié avant toute collecte
      de données. Le mot de passe est haché via bcrypt avant stockage.
    - ``POST /login``           — Authentification par identifiant et mot
      de passe. Retourne un token JWT (Bearer) à inclure dans l'en-tête
      ``Authorization`` des requêtes ultérieures.
    - ``GET /me``               — Récupération des informations de
      l'utilisateur actuellement authentifié (dérivées du token JWT).
    - ``GET /export``           — Export des données personnelles de
      l'utilisateur connecté au format JSON (RGPD Art. 15 — droit
      d'accès & Art. 20 — droit à la portabilité).
    - ``DELETE /delete-account``— Suppression définitive du compte et de
      toutes les données personnelles associées (RGPD Art. 17 — droit
      à l'effacement / « droit à l'oubli »).

Conformité RGPD :
    - **Art. 6.1.a — Consentement** : L'inscription est conditionnée à
      l'acceptation explicite de la politique de confidentialité.
    - **Art. 7 — Preuve du consentement** : La date UTC du consentement
      est horodatée dans le champ ``gdpr_consent_at``.
    - **Art. 15 — Droit d'accès** : L'endpoint ``/export`` permet à
      l'utilisateur de consulter l'ensemble de ses données personnelles.
    - **Art. 17 — Droit à l'effacement** : L'endpoint ``/delete-account``
      supprime définitivement le compte et les données associées.
    - **Art. 20 — Droit à la portabilité** : L'export est fourni dans
      un format structuré, couramment utilisé et lisible par machine (JSON).
    - **Art. 32 — Sécurité du traitement** : Les mots de passe sont
      hachés via bcrypt et ne sont jamais exposés en réponse API.

Rate Limiting :
    Certains endpoints (``/register``, ``/login``) sont protégés par un
    mécanisme de rate limiting (slowapi) afin de prévenir les attaques
    par force brute et les inscriptions automatisées.

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, status, Request, File, UploadFile , Response
from sqlalchemy.orm import Session
from fastapi.security import OAuth2PasswordRequestForm
from app.database import get_db, User, Prediction, UserPicture
from app import crud, schemas
from app.auth import create_access_token, verify_password, get_current_active_user
from app.limiter import limiter

# Instance du routeur FastAPI pour les endpoints d'authentification.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/account" et le tag "Account" pour la documentation OpenAPI.
router = APIRouter()

# ================== LOGIN ==================


@router.post("/login", response_model=schemas.Token)
@limiter.limit("10/minute")
def login(
    request: Request,
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
        request (Request): Objet de requête FastAPI, pour extraire l'adresse IP du client pour
            le système de rate limiting.
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
@limiter.limit("5/minute")
def register(
    request: Request,
    user: schemas.UserCreate,
    db: Session = Depends(get_db),
):
    """
    Inscrire un nouvel utilisateur sur la plateforme SnapTaPlaque.

    Crée un compte utilisateur après validation du consentement RGPD
    et vérification de l'unicité de l'adresse email. Le mot de passe
    est haché via bcrypt avant persistance en base de données (voir
    ``crud.create_user``). La date du consentement RGPD est horodatée
    automatiquement lors de la création du compte.

    Le rate limiting est fixé à 5 requêtes par minute par adresse IP
    afin de prévenir les inscriptions automatisées (bots, brute-force).

    Conformité RGPD :
        - **Art. 6.1.a — Consentement** : L'inscription est refusée
          si le champ ``gdpr_consent`` est ``False``. Le consentement
          explicite est une condition préalable obligatoire.
        - **Art. 7 — Preuve du consentement** : La date et l'heure UTC
          du consentement sont enregistrées dans le champ
          ``gdpr_consent_at`` du modèle ``User`` (voir ``crud.create_user``).
        - **Art. 5.1.c — Minimisation des données** : Seules les données
          strictement nécessaires à la création du compte sont collectées
          (email, username, password, full_name).

    Args:
        request (Request): Objet de requête FastAPI, utilisé par le
            système de rate limiting (``slowapi``) pour identifier
            l'adresse IP du client.
        user (schemas.UserCreate): Schéma Pydantic contenant les
            données d'inscription validées :
            - ``email`` (str) : Adresse email unique.
            - ``username`` (str) : Nom d'utilisateur unique.
            - ``password`` (str) : Mot de passe en clair (haché avant
              stockage).
            - ``full_name`` (str) : Nom complet de l'utilisateur.
            - ``is_admin`` (bool) : Statut administrateur.
            - ``gdpr_consent`` (bool) : Consentement RGPD explicite.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        schemas.UserResponse: Informations publiques de l'utilisateur
            nouvellement créé (id, email, username, full_name, is_active,
            is_admin, created_at). Le mot de passe haché est exclu de
            la réponse.

    Raises:
        HTTPException (400): Si le consentement RGPD n'est pas donné
            (``gdpr_consent == False``) ou si l'adresse email est déjà
            associée à un compte existant.
        HTTPException (429): Si la limite de 5 inscriptions par minute
            par adresse IP est dépassée (rate limiting).
    """
    # Vérification du consentement RGPD (Art. 6.1.a) — condition
    # préalable obligatoire avant toute collecte de données personnelles.
    if not user.gdpr_consent:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Vous devez accepter la politique de confidentialité (RGPD).",
        )
    # Vérification de l'unicité de l'adresse email pour éviter les
    # doublons en base de données.
    db_user = crud.get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email déjà enregistré")
    # Création de l'utilisateur en base avec hachage du mot de passe
    # et horodatage du consentement RGPD (voir crud.create_user).
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


@router.get("/me/data-export")
def export_my_data(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
):
    """
    Exporter l'ensemble des données personnelles de l'utilisateur connecté.

    Endpoint RGPD — Droit d'accès (Art. 15) et droit à la portabilité
    (Art. 20). Retourne un fichier JSON structuré contenant toutes les
    données personnelles détenues par la plateforme pour l'utilisateur
    authentifié : informations de profil, historique des prédictions
    et liste des véhicules favoris.

    Returns:
        dict: Dictionnaire contenant les clés ``profile``,
            ``predictions`` et ``favorites``.
    """
    predictions = crud.get_user_predictions(db, current_user.id, skip=0, limit=10000)
    favorites = crud.get_user_favorites(db, current_user.id)

    return {
        "profile": {
            "id": current_user.id,
            "username": current_user.username,
            "email": current_user.email,
            "full_name": current_user.full_name,
            "is_active": current_user.is_active,
            "created_at": current_user.created_at.isoformat() if current_user.created_at else None,
            "gdpr_consent_at": current_user.gdpr_consent_at.isoformat() if current_user.gdpr_consent_at else None,
        },
        "predictions": [
            {
                "id": p.id,
                "filename": p.filename,
                "results": p.results,
                "created_at": p.created_at.isoformat() if p.created_at else None,
            }
            for p in predictions
        ],
        "favorites": [
            {
                "license_plate": v.license_plate,
                "brand": v.brand,
                "model": v.model,
                "info": v.info,
                "energy": v.energy,
            }
            for v in favorites
        ],
    }

@router.delete("/me/delete-account")
def delete_my_account(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
):
    """
    Supprimer le compte et toutes les données personnelles de l'utilisateur.

    Endpoint RGPD — Droit à l'effacement (Art. 17 du Règlement Général
    sur la Protection des Données). Supprime de manière irréversible
    l'ensemble des données associées à l'utilisateur authentifié :
    favoris, prédictions et profil utilisateur.

    L'ensemble des suppressions est effectué au sein d'une même
    transaction SQLAlchemy. Le ``commit`` final garantit l'atomicité
    de l'opération : soit toutes les données sont supprimées, soit
    aucune ne l'est en cas d'erreur.

    .. warning::
        Cette opération est **irréversible**. Aucune sauvegarde ni
        anonymisation n'est effectuée.

    Args:
        current_user (User): Utilisateur authentifié dont le compte
            doit être supprimé, injecté par la dépendance
            ``get_current_active_user``. Déclenche une erreur HTTP 401
            si le token JWT est absent, expiré ou invalide.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``. Utilisée pour exécuter les
            requêtes de suppression et valider la transaction.

    Returns:
        dict: Dictionnaire contenant la clé ``message`` avec un texte
            de confirmation de la suppression définitive du compte
            et des données personnelles.

    Raises:
        HTTPException (401): Si l'utilisateur n'est pas authentifié
            ou si son compte est désactivé (via ``get_current_active_user``).
    """
    from app.database import user_favorites
    db.execute(
        user_favorites.delete().where(user_favorites.c.user_id == current_user.id)
    )
    db.query(Prediction).filter(Prediction.user_id == current_user.id).delete()
    db.query(User).filter(User.id == current_user.id).delete()
    db.commit()

    return {"message": "Compte et données personnelles supprimés définitivement."}


@router.get("/me/profile-picture")
def get_profile_picture(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
):
    """
    Récupérer la photo de profil de l'utilisateur connecté.
    Retourne directement le fichier image binaire.
    """
    user_picture = db.query(UserPicture).filter(UserPicture.user_id == current_user.id).first()

    if not user_picture or not user_picture.picture:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, 
            detail="Photo de profil non trouvée"
        )

    # On renvoie directement les octets binaires de l'image.
    # Assure-toi de mettre le bon 'media_type' (ex: "image/jpeg" ou "image/png").
    return Response(content=user_picture.picture, media_type="image/jpeg")

@router.post("/me/change-profile-picture")
async def change_profile_picture(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
    file: UploadFile = File(...),
):
    # Lecture des bytes de l'image
    image_bytes = await file.read()
    
    user_picture = db.query(UserPicture).filter(UserPicture.user_id == current_user.id).first()

    if not user_picture:
        user_picture = UserPicture(user_id=current_user.id)
        db.add(user_picture)

    user_picture.picture = image_bytes
    db.commit()
    
    return {"message": "Photo de profil modifiée avec succès."}

@router.delete("/me/delete-profile-picture")
def delete_profile_picture(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
):
    """
    Supprimer la photo de profil de l'utilisateur connecté.

    Args:
        current_user (User): Utilisateur authentifié dont la photo de
            profil doit être supprimée, injecté par la dépendance
            ``get_current_active_user``. Déclenche une erreur HTTP 401
            si le token JWT est absent, expiré ou invalide.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``. Utilisée pour supprimer le champ
            ``picture`` du modèle ``UserPicture`` associé à
            l'utilisateur.

    Returns:
        dict: Dictionnaire contenant la clé ``message`` avec un texte
            de confirmation de la suppression de la photo de profil.
    """
    user_picture = db.query(UserPicture).filter(UserPicture.user_id == current_user.id).first()

    if user_picture and user_picture.picture:
        user_picture.picture = None
        db.commit()
    
    return {"message": "Photo de profil supprimée avec succès."}