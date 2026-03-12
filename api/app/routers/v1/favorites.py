"""
favorites.py — Routeur de gestion des favoris pour l'API SnapTaPlaque.

Ce module définit les endpoints permettant aux utilisateurs authentifiés
de gérer leur liste de véhicules favoris. Chaque endpoint est protégé
par la dépendance ``get_current_user`` qui vérifie la validité du token
JWT présent dans l'en-tête ``Authorization`` de la requête.

Endpoints exposés :
    - ``POST /add``      — Ajouter un véhicule à la liste des favoris de
      l'utilisateur connecté, identifié par sa plaque d'immatriculation.
    - ``DELETE /remove``  — Retirer un véhicule de la liste des favoris de
      l'utilisateur connecté.
    - ``GET /all``        — Récupérer l'ensemble des véhicules favoris de
      l'utilisateur connecté.

Ces endpoints sont montés sous le préfixe ``/favorites`` par le routeur
principal de l'application (voir ``app/main.py``).

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app import crud, schemas
from app.database import get_db, User
from app.auth import get_current_user
from app.schemas import AllFavoritesResponse

# Instance du routeur FastAPI pour les endpoints de gestion des favoris.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/favorites" et le tag "Favorites" pour la documentation OpenAPI.
router = APIRouter()

# ================== ADD FAVORITE ==================


@router.post("/add")
def add_favorite(
    license_plate: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Ajouter un véhicule aux favoris de l'utilisateur connecté.

    Recherche le véhicule correspondant à la plaque d'immatriculation
    fournie en paramètre de requête. Si le véhicule existe en base de
    données, il est ajouté à la liste des favoris de l'utilisateur
    authentifié. Si le véhicule est déjà présent dans les favoris,
    une erreur HTTP 400 est retournée.

    Args:
        license_plate (str): Plaque d'immatriculation du véhicule à
            ajouter aux favoris, transmise en paramètre de requête.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_user``. Déclenche une erreur HTTP 401 si le
            token est absent, expiré ou invalide.

    Returns:
        dict: Message de confirmation contenant la clé ``message``.

    Raises:
        HTTPException (404): Si aucun véhicule ne correspond à la plaque
            d'immatriculation fournie.
        HTTPException (400): Si le véhicule est déjà présent dans la
            liste des favoris de l'utilisateur.
    """
    vehicle = crud.get_vehicle_by_license_plate(db, license_plate)
    if not vehicle:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Véhicule non trouvé."
        )
    try:
        crud.add_favorite(db, current_user.id, license_plate)
    except Exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ce véhicule est déjà dans vos favoris."
        )
    return {"message": "Véhicule ajouté aux favoris."}

# ================== REMOVE FAVORITE ==================


@router.delete("/remove")
def remove_favorite(
    license_plate: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Retirer un véhicule des favoris de l'utilisateur connecté.

    Supprime l'association entre l'utilisateur authentifié et le véhicule
    identifié par la plaque d'immatriculation fournie en paramètre de
    requête. Si le véhicule n'était pas dans les favoris, l'opération
    est silencieusement ignorée.

    Args:
        license_plate (str): Plaque d'immatriculation du véhicule à
            retirer des favoris, transmise en paramètre de requête.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_user``. Déclenche une erreur HTTP 401 si le
            token est absent, expiré ou invalide.

    Returns:
        dict: Message de confirmation contenant la clé ``message``.
    """
    crud.remove_favorite(db, current_user.id, license_plate)
    return {"message": "Véhicule retiré des favoris."}

# ================== GET ALL FAVORITES ==================


@router.get("/all", response_model=AllFavoritesResponse)
def get_favorites(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Récupérer la liste des véhicules favoris de l'utilisateur connecté.

    Retourne l'ensemble des véhicules que l'utilisateur authentifié a
    ajoutés à ses favoris, sérialisés selon le schéma
    ``VehicleInfoResponse``. Si l'utilisateur n'a aucun favori, une
    liste vide est retournée.

    Args:
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_user``. Déclenche une erreur HTTP 401 si le
            token est absent, expiré ou invalide.

    Returns:
        Liste des véhicules favoris de
            l'utilisateur avec leurs informations complètes (plaque,
            marque, modèle, année, etc.).
    """
    return  {
            "favorites":
                crud.get_user_favorites(db, current_user.id)
            }
