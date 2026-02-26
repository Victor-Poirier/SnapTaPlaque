"""
model.py — Routeur d'informations sur le modèle de reconnaissance de plaques.

Ce module expose un endpoint permettant de consulter l'état et les
caractéristiques du pipeline de reconnaissance de plaques d'immatriculation
(LPR) utilisé par l'API SnapTaPlaque.

Endpoint exposé :
    - ``GET /info`` — Retourne les métadonnées du modèle de détection
      et de reconnaissance (type de modèle, statut de chargement,
      nom du pipeline).

Cet endpoint est monté sous le préfixe ``/model`` par le routeur
principal de l'application (voir ``app/main.py``).

Dépendances :
    - ``app.predictor.plate_predictor`` — Instance singleton du pipeline
      de prédiction LPR combinant YOLOv8 (détection de plaques) et
      EasyOCR (extraction du texte).

Version : 1.0.0
"""

from fastapi import APIRouter, HTTPException
from app.predictor import plate_predictor
import logging

# Instance du routeur FastAPI pour les endpoints liés au modèle.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/model" et le tag "Model" pour la documentation OpenAPI.
router = APIRouter()

# Logger dédié à ce module, utilisant le nom qualifié du fichier
# pour faciliter le filtrage et le diagnostic dans les journaux.
logger = logging.getLogger(__name__)


@router.get("/info")
async def get_model_info():
    """
    Récupérer les informations et l'état du modèle de reconnaissance de plaques.

    Interroge l'instance singleton ``plate_predictor`` pour déterminer si
    le pipeline LPR est correctement chargé en mémoire, puis retourne un
    dictionnaire décrivant le type de modèle et le nom du pipeline.

    Cet endpoint est utile pour les vérifications de santé (health checks)
    et les tableaux de bord de supervision afin de s'assurer que le modèle
    est opérationnel avant de soumettre des requêtes de prédiction.

    Returns:
        dict: Dictionnaire contenant les clés suivantes :
            - ``loaded`` (bool) : ``True`` si le modèle est chargé en
              mémoire et prêt à effectuer des prédictions.
            - ``model_type`` (str) : Description du pipeline utilisé
              (``"YOLOv8 + EasyOCR"``).
            - ``pipeline`` (str) : Nom interne du pipeline
              (``"LPRPipeline"``).

    Raises:
        HTTPException (500) : Si une erreur inattendue survient lors de
            l'interrogation du prédicteur (modèle non initialisé,
            dépendance manquante, etc.).
    """
    try:
        return {
            "loaded": plate_predictor.is_loaded(),
            "model_type": "YOLOv8 + EasyOCR",
            "pipeline": "LPRPipeline"
        }
    except Exception as e:
        logger.error(f"Erreur modèle: {str(e)}")
        raise HTTPException(status_code=500, detail="Cannot retrieve model info")
