"""
predictions.py — Routeur de prédiction pour l'API SnapTaPlaque.

Ce module définit les endpoints liés à la détection et la reconnaissance
de plaques d'immatriculation à partir d'images uploadées par les
utilisateurs. Chaque endpoint est protégé par la dépendance
``get_current_active_user`` qui vérifie la validité du token JWT
présent dans l'en-tête ``Authorization`` de la requête.

Endpoints exposés :
    - ``POST /predict``  — Soumettre une image pour détection et
      reconnaissance de plaque d'immatriculation via le pipeline
      YOLO + EasyOCR. Retourne le texte de la plaque détectée ainsi
      que le score de confiance associé.
    - ``GET /history``   — Consulter l'historique des prédictions
      effectuées par l'utilisateur connecté, avec pagination.
    - ``GET /stats``     — Récupérer les statistiques agrégées des
      prédictions de l'utilisateur connecté (nombre total, taux de
      détection, etc.).

Ces endpoints sont montés sous le préfixe ``/predictions`` par le
routeur principal de l'application (voir ``app/main.py``).

Version : 1.0.0
"""

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db, User
from app.auth import get_current_active_user
from app.crud import create_prediction, get_user_predictions, get_user_prediction_stats
from app.predictor import plate_predictor
from app.schemas import PlateHistory, PlateStats

# Instance du routeur FastAPI pour les endpoints de prédiction.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/predictions" et le tag "Predictions" pour la documentation
# OpenAPI.
router = APIRouter()

# ================== PREDICT ==================


@router.post("/predict")
async def predict_plate(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """
    Soumettre une image pour détection de plaque d'immatriculation.

    Reçoit un fichier image (JPEG, PNG, etc.) via un formulaire
    ``multipart/form-data``, le transmet au pipeline de détection
    YOLO + EasyOCR, puis persiste le résultat en base de données
    au nom de l'utilisateur authentifié.

    Le pipeline procède en deux étapes :
        1. **Détection** — Le modèle YOLO localise la zone de la plaque
           dans l'image.
        2. **Reconnaissance** — EasyOCR extrait le texte alphanumérique
           de la zone détectée.

    Si aucune plaque n'est détectée, la prédiction est tout de même
    enregistrée en base avec une liste de résultats vide.

    Args:
        file (UploadFile): Fichier image uploadé par le client, injecté
            automatiquement par FastAPI via ``File(...)``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_active_user``. Déclenche une erreur HTTP 401
            si le token est absent, expiré ou invalide.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        dict: Dictionnaire contenant ``filename`` (nom du fichier
            soumis), ``results`` (liste des plaques détectées avec
            ``plate_text`` et ``confidence``) et ``prediction_id``
            (identifiant unique de la prédiction en base).

    Raises:
        HTTPException (503): Si le modèle de prédiction n'a pas encore
            été chargé en mémoire.
        HTTPException (400): Si le fichier fourni n'est pas une image
            valide exploitable par le pipeline de détection.
    """
    contents = await file.read()

    if not plate_predictor.is_loaded():
        raise HTTPException(status_code=503, detail="Modèle non chargé")

    try:
        result = plate_predictor.predict(contents)
    except ValueError:
        raise HTTPException(status_code=400, detail="Image invalide")

    results = [result] if result["plate_text"] else []

    prediction = create_prediction(
        db=db,
        user_id=current_user.id,
        filename=file.filename,
        results=results
    )

    return {
        "filename": file.filename,
        "results": results,
        "prediction_id": prediction.id
    }

# ================== HISTORY ==================


@router.get("/history", response_model=List[PlateHistory])
async def get_prediction_history(
    skip: int = 0,
    limit: int = 100,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """
    Consulter l'historique des prédictions de l'utilisateur connecté.

    Retourne la liste des prédictions effectuées par l'utilisateur
    authentifié, ordonnées chronologiquement. Chaque prédiction contenant
    un ou plusieurs résultats de détection est éclatée en autant d'entrées
    individuelles dans la réponse. Les prédictions sans détection sont
    incluses avec des valeurs ``null`` pour ``plate_text`` et
    ``confidence``.

    La pagination est gérée via les paramètres ``skip`` et ``limit``
    qui s'appliquent au niveau des prédictions en base (et non au niveau
    des entrées individuelles retournées).

    Args:
        skip (int): Nombre de prédictions à ignorer depuis le début de
            la liste (offset). Par défaut ``0``.
        limit (int): Nombre maximum de prédictions à récupérer. Par
            défaut ``100``.
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_active_user``. Déclenche une erreur HTTP 401
            si le token est absent, expiré ou invalide.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        list[PlateHistory]: Liste des entrées d'historique contenant
            chacune ``id``, ``plate_text``, ``confidence`` et
            ``created_at``.
    """
    predictions = get_user_predictions(db, current_user.id, skip=skip, limit=limit)
    history = []
    for pred in predictions:
        if pred.results and len(pred.results) > 0:
            for result in pred.results:
                history.append(PlateHistory(
                    id=pred.id,
                    plate_text=result.get("plate_text"),
                    confidence=result.get("confidence"),
                    created_at=pred.created_at
                ))
        else:
            history.append(PlateHistory(
                id=pred.id,
                plate_text=None,
                confidence=None,
                created_at=pred.created_at
            ))
    return history

# ================== STATS ==================


@router.get("/stats", response_model=PlateStats)
async def get_prediction_statistics(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    """
    Récupérer les statistiques de prédiction de l'utilisateur connecté.

    Retourne des métriques agrégées sur l'ensemble des prédictions
    effectuées par l'utilisateur authentifié : nombre total de
    prédictions, nombre de détections réussies, taux de détection, etc.

    Ces statistiques sont utiles pour les tableaux de bord côté
    front-end permettant à l'utilisateur de suivre son activité sur
    la plateforme.

    Args:
        current_user (User): Utilisateur authentifié, injecté par
            ``get_current_active_user``. Déclenche une erreur HTTP 401
            si le token est absent, expiré ou invalide.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        PlateStats: Statistiques agrégées des prédictions de
            l'utilisateur (total, réussies, taux de détection, etc.).
    """
    return get_user_prediction_stats(db, current_user.id)