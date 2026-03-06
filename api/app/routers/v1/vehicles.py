"""
vehicles.py — Routeur de consultation des véhicules pour l'API SnapTaPlaque.

Ce module définit les endpoints permettant aux utilisateurs authentifiés
de consulter les informations détaillées d'un véhicule à partir de sa
plaque d'immatriculation. Chaque endpoint est protégé par la dépendance
``get_current_user`` qui vérifie la validité du token JWT présent dans
l'en-tête ``Authorization`` de la requête.

Endpoints exposés :
    - ``GET /info`` — Récupérer les informations complètes d'un véhicule
      (marque, modèle, année, couleur, etc.) à partir de sa plaque
      d'immatriculation fournie en paramètre de requête.

Ces endpoints sont montés sous le préfixe ``/vehicles`` par le routeur
principal de l'application (voir ``app/main.py``).

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app import crud, schemas
from app.database import get_db, User
from app.auth import get_current_user

# Instance du routeur FastAPI pour les endpoints de consultation des véhicules.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/vehicles" et le tag "Vehicles" pour la documentation OpenAPI.
router = APIRouter()

# ================== VEHICLE INFO ==================


@router.get("/info", response_model=schemas.VehicleInfoResponse)
def get_vehicle_info(
    license_plate: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """
    Récupérer les informations d'un véhicule par sa plaque d'immatriculation.

    Recherche en base de données le véhicule correspondant à la plaque
    d'immatriculation fournie en paramètre de requête. La plaque est
    nettoyée (suppression des espaces et guillemets superflus) avant la
    recherche afin de tolérer les variations de formatage côté client.

    Cet endpoint est typiquement appelé après une détection réussie via
    le endpoint ``/predictions/predict`` pour enrichir le résultat OCR
    avec les informations complètes du véhicule.

    Args:
        license_plate (str): Plaque d'immatriculation du véhicule à
            rechercher, transmise en paramètre de requête (query string).
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_user``. Déclenche une erreur HTTP 401 si le
            token est absent, expiré ou invalide.

    Returns:
        schemas.VehicleInfoResponse: Informations complètes du véhicule
            (plaque, marque, modèle, année, couleur, etc.).

    Raises:
        HTTPException (404): Si aucun véhicule ne correspond à la plaque
            d'immatriculation fournie après nettoyage.
    """
    cleaned_plate = license_plate.strip('" ')
    print(f"Requête d'information pour la plaque d'immatriculation : '{cleaned_plate}' par l'utilisateur {current_user.username}")
    vehicle = crud.get_vehicle_by_license_plate(db, cleaned_plate)
    if not vehicle:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Véhicule non trouvé pour la plaque d'immatriculation fournie."
        )
    return vehicle