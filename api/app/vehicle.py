import httpx
import logging
from typing import Optional, Dict, Any, Tuple


# Configuration du logger
logger = logging.getLogger(__name__)

# Client HTTP global pour les appels externes
http_client = httpx.Client(timeout=10.0)

# ================== UTILITAIRES DE SERVICE ==================
def _get_oscaro_csrf_token() -> Tuple[Optional[str], Optional[httpx.Cookies]]:
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
        response = http_client.get(url, headers=headers)
        response.raise_for_status()
        data = response.json()
        return data.get('csrf-token'), response.cookies
    except Exception as e:
        logger.error(f"Erreur lors de la récupération du token CSRF: {e}")
        return None, None

def _fetch_vehicle_data_from_provider(plate: str) -> Optional[Dict[str, Any]]:
    """
    Interroge le service tiers pour obtenir les caractéristiques techniques.

    La plaque est formatée pour correspondre aux attentes du fournisseur.

    :param plate: Plaque d'immatriculation nettoyée (ex: "AB123CD").
    :type plate: str
    :returns: Dictionnaire des données véhicule ou None si non trouvé.
    :rtype: Optional[Dict[str, Any]]
    """
    csrf_token, cookies = _get_oscaro_csrf_token()

    if not csrf_token:
        return None

    url = "https://www.oscaro.com/xhr/dionysos-search/fr/fr"
    headers = {
        'accept': 'application/json',
        'referer': 'https://www.oscaro.com/',
        'x-csrf-token': csrf_token,
        'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
    params = {'plate': plate}

    try:
        response = http_client.get(url, headers=headers, params=params, cookies=cookies)

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
            'license_plate': plate,
            'brand': brand,
            'model': model,
            'info': vehicle_raw.get('labels', {}).get('complement-label', {}).get('fr', ''),
            'energy': vehicle_raw.get('energy', {}).get('label', {}).get('fr', '') or vehicle_raw.get('energy', ''),
        }

    except Exception as e:
        logger.error(f"Erreur lors de la récupération des infos véhicule ({plate}): {e}")
        return None
