"""
vehicles.py — Routeur de consultation des véhicules pour l'API SnapTaPlaque.

Ce module définit les endpoints permettant aux utilisateurs authentifiés
de consulter les informations détaillées d'un véhicule à partir de sa
plaque d'immatriculation via un service tiers (Oscaro).

Version : 1.1.0
"""

import logging
import httpx
from typing import Optional, Dict, Any, Tuple
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app import crud, schemas
from app.database import get_db, User
from app.auth import get_current_user

# Configuration du logger
logger = logging.getLogger(__name__)

router = APIRouter()

# Client HTTP asynchrone global pour réutiliser les connexions (Pooling)
# Dans une application de production, on l'initialiserait via le lifespan de FastAPI
http_client = httpx.AsyncClient(timeout=10.0)

# ================== UTILITAIRES DE SERVICE ==================

async def _get_oscaro_csrf_token() -> Tuple[Optional[str], Optional[httpx.Cookies]]:
    """
    Récupère le token CSRF et les cookies nécessaires auprès d'Oscaro.

    :returns: Un tuple contenant le token CSRF et les cookies de session.
    :rtype: Tuple[Optional[str], Optional[httpx.Cookies]]
    :raises httpx.HTTPError: En cas de problème de connexion au service tiers.
    """
    url = "https://www.oscaro.com/xhr/init-client"
    headers = {
        'accept': 'application/json',
        'referer': 'https://www.oscaro.com/',
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }

    try:
        response = await http_client.get(url, headers=headers)
        response.raise_for_status()
        data = response.json()
        return data.get('csrf-token'), response.cookies
    except Exception as e:
        logger.error(f"Erreur lors de la récupération du token CSRF: {e}")
        return None, None

async def _fetch_vehicle_data_from_provider(plate: str) -> Optional[Dict[str, Any]]:
    """
    Interroge le service tiers pour obtenir les caractéristiques techniques.

    La plaque est formatée pour correspondre aux attentes du fournisseur.

    :param plate: Plaque d'immatriculation nettoyée (ex: "AB123CD").
    :type plate: str
    :returns: Dictionnaire des données véhicule ou None si non trouvé.
    :rtype: Optional[Dict[str, Any]]
    """
    csrf_token, cookies = await _get_oscaro_csrf_token()

    if not csrf_token:
        return None

    # Formatage SIV pour l'affichage (AB-123-CD)
    plate_formatted = f"{plate[:2]}-{plate[2:5]}-{plate[5:]}" if len(plate) == 7 else plate

    url = "https://www.oscaro.com/xhr/dionysos-search/fr/fr"
    headers = {
        'accept': 'application/json',
        'referer': 'https://www.oscaro.com/',
        'x-csrf-token': csrf_token,
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    params = {'plate': plate}

    try:
        response = await http_client.get(url, headers=headers, params=params, cookies=cookies)

        if response.status_code != 200:
            return None

        data = response.json()
        vehicle_raw = None

        # Extraction selon la structure de réponse
        if 'vehicles' in data and data['vehicles']:
            vehicle_raw = data['vehicles'][0]
        elif 'vehicle' in data:
            vehicle_raw = data['vehicle']
        elif isinstance(data, list) and data:
            vehicle_raw = data[0]

        if not vehicle_raw:
            return None

        # Parsing des labels
        core_label = vehicle_raw.get('labels', {}).get('core-label', {}).get('fr', '')
        parts = core_label.split(' ')

        brand = parts[0] if parts else "Inconnu"
        model = ' '.join(parts[1:]) if len(parts) > 1 else ""

        return {
            'licence_plate': plate_formatted,
            'brand': brand,
            'model': model,
            'info': vehicle_raw.get('labels', {}).get('complement-label', {}).get('fr', ''),
            'energy': vehicle_raw.get('energy', {}).get('label', {}).get('fr', '') or vehicle_raw.get('energy', ''),
            'success': True
        }

    except Exception as e:
        logger.error(f"Erreur lors de la récupération des infos véhicule ({plate}): {e}")
        return None

# ================== ENDPOINTS ==================

@router.get("/info", response_model=schemas.VehicleInfoResponse)
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

    logger.info(f"Requête plaque: '{clean_plate}' par user: {current_user.username}")

    # 2. Appel au service de récupération asynchrone
    vehicle_data = await _fetch_vehicle_data_from_provider(clean_plate)

    if not vehicle_data:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Véhicule non trouvé ou service tiers indisponible."
        )

    # 3. Retourne les données mappées sur le schéma Pydantic
    return vehicle_data