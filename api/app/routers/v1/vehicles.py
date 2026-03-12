"""
vehicles.py — Routeur de consultation des véhicules pour l'API SnapTaPlaque.

Ce module définit les endpoints permettant aux utilisateurs authentifiés
de consulter les informations détaillées d'un véhicule à partir de sa
plaque d'immatriculation via un service tiers (Oscaro).

Version : 1.1.0
"""

import logging

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app import crud, schemas
from app.database import get_db, User
from app.auth import get_current_user

from app.vehicle import _fetch_vehicle_data_from_provider

# Configuration du logger
logger = logging.getLogger(__name__)

router = APIRouter()


# ================== ENDPOINTS ==================

@router.post("/info", response_model=schemas.VehicleInfoResponse)
async def get_vehicle_info(
        license_plate: str,
        db: Session = Depends(get_db),
        current_user: User = Depends(get_current_user),
):
    """
    Récupère les informations détaillées d'un véhicule.

    Ce service effectue une recherche asynchrone pour identifier les caractéristiques
    techniques d'un véhicule (marque, modèle, énergie) à partir de sa plaque.

    :param license_plate: Plaque d'immatriculation (format libre).
    :type license_plate: str
    :param db: Session de base de données SQLAlchemy.
    :type db: Session
    :param current_user: Utilisateur extrait du token JWT.
    :type current_user: User
    :raises HTTPException 404: Si le véhicule est introuvable.
    :raises HTTPException 503: Si le service tiers est indisponible.
    :returns: Objet contenant les détails du véhicule.
    :rtype: schemas.VehicleInfoResponse
    """
    # 1. Nettoyage de la plaque
    clean_plate = license_plate.replace('-', '').replace(' ', '').replace('"', '').upper()
    plate_formatted = f"{clean_plate[:2]}-{clean_plate[2:5]}-{clean_plate[5:]}" if len(clean_plate) == 7 else clean_plate
    logger.info(f"Requête plaque: '{plate_formatted}' par user: {current_user.username}")

    try:
        vehicle = crud.get_vehicle_by_license_plate(db, plate_formatted)
        print(f"Véhicule trouvé en bdd: {vehicle}")
        if not vehicle:
            logger.info(f"Véhicule '{plate_formatted}' non trouvé dans la bdd, appel de l'API Oscaro.")
            # 2. Appel au service de récupération asynchrone
            vehicle = _fetch_vehicle_data_from_provider(plate_formatted)

            if not vehicle:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Véhicule non trouvé ou API Oscaro indisponible."
                )
            else:
                # Sauvegarde dans la base de données
                crud.create_vehicle(db, vehicle)
                # Enregistrement de l'historique de consultation pour l'utilisateur
                crud.create_vehicle_info_history(db, current_user.id, license_plate=plate_formatted)
                logger.info(f"Véhicule '{plate_formatted}' récupéré et sauvegardé en bdd.")


        # 3. Retourne les données mappées sur le schéma Pydantic
        print(f"Véhicule trouvé: {vehicle}")
        return vehicle

    except HTTPException as e:
        logger.error(f"Erreur lors de la récupération des données du véhicule: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Erreur lors de la récupération des données du véhicule."
        )
    
@router.get("/history", response_model=schemas.VehicleInfoHistoryResponse)
async def get_vehicle_info_history(
        db: Session = Depends(get_db),
        current_user: User = Depends(get_current_user),
):
    """
    Récupère l'historique des informations de véhicules consultés ou enregistrés par l'utilisateur.

    Ce service retourne une liste d'entrées d'historique, chacune contenant les détails d'un véhicule
    et la date de consultation/enregistrement.

    :param db: Session de base de données SQLAlchemy.
    :type db: Session
    :param current_user: Utilisateur extrait du token JWT.
    :type current_user: User
    :returns: Liste d'entrées d'historique des véhicules.
    :rtype: schemas.VehicleInfoHistoryResponse
    """
    try:
        history_entries = crud.get_vehicle_info_history_by_user(db, current_user.id)
        return schemas.VehicleInfoHistoryResponse(history=history_entries)
    
    except Exception as e:
        logger.error(f"Erreur lors de la récupération de l'historique des véhicules: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Erreur lors de la récupération de l'historique des véhicules."
        )  