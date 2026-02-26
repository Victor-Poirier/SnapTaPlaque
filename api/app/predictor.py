"""
predictor.py — Wrapper de prédiction pour l'intégration du pipeline LPR avec l'API FastAPI.

Ce module encapsule le pipeline de reconnaissance de plaques
d'immatriculation (``LPRPipeline``) dans une classe adaptée à
l'écosystème FastAPI. Il fournit une interface simplifiée pour le
chargement du modèle, la vérification de son état et l'exécution
des prédictions à partir de données binaires d'image.

Composants exposés :
    - ``PlatePredictor``     — Classe encapsulant le pipeline LPR,
      offrant des méthodes de chargement, de vérification d'état et
      de prédiction.
    - ``plate_predictor``    — Instance singleton globale de
      ``PlatePredictor``, partagée par l'ensemble de l'application
      FastAPI.

Flux de traitement :
    1. L'image est reçue sous forme de bytes bruts depuis l'endpoint
       FastAPI.
    2. Les bytes sont décodés en tableau NumPy puis convertis en image
       OpenCV (BGR).
    3. L'image est transmise au pipeline ``LPRPipeline`` qui exécute
       la détection YOLO puis la lecture OCR.
    4. Le meilleur résultat (confiance maximale) est sélectionné et
       retourné sous forme de dictionnaire normalisé.

Version : 1.0.0
"""

import cv2
import numpy as np
from app.model.lpr_engine import LPRPipeline


class PlatePredictor:
    """
    Wrapper autour de LPRPipeline pour l'intégration avec l'API FastAPI.

    Encapsule le pipeline de reconnaissance de plaques d'immatriculation
    dans une interface simplifiée adaptée au cycle de vie de l'application
    FastAPI. Le modèle est chargé de manière explicite via ``load_model``
    et son état peut être vérifié via ``is_loaded`` avant toute prédiction.

    Attributes:
        pipeline (LPRPipeline | None): Instance du pipeline LPR chargée
            en mémoire. ``None`` si le modèle n'a pas encore été chargé
            ou si le chargement a échoué.
    """

    def __init__(self):
        """
        Initialiser le prédicteur sans chargement de modèle.

        Le pipeline est initialisé à ``None``. Le chargement effectif
        du modèle doit être déclenché explicitement via l'appel à
        ``load_model()``, typiquement lors de l'événement de démarrage
        de l'application FastAPI.
        """
        self.pipeline = None

    def load_model(self) -> bool:
        """
        Charger les modèles YOLO et EasyOCR via LPRPipeline.

        Instancie le pipeline ``LPRPipeline`` qui charge en mémoire le
        modèle de détection YOLO et le moteur de reconnaissance optique
        de caractères EasyOCR. En cas d'échec (modèle introuvable,
        erreur de dépendance, mémoire insuffisante), l'attribut
        ``pipeline`` reste à ``None`` et l'erreur est journalisée.

        Returns:
            bool: ``True`` si le chargement du pipeline a réussi,
                ``False`` en cas d'erreur.
        """
        try:
            self.pipeline = LPRPipeline()
            return True
        except Exception as e:
            print(f"Erreur chargement modèle: {e}")
            self.pipeline = None
            return False

    def is_loaded(self) -> bool:
        """
        Vérifier si le pipeline de reconnaissance est chargé en mémoire.

        Permet aux endpoints et aux dépendances FastAPI de s'assurer que
        le modèle est opérationnel avant de soumettre une image pour
        prédiction.

        Returns:
            bool: ``True`` si le pipeline est chargé et prêt à recevoir
                des prédictions, ``False`` sinon.
        """
        return self.pipeline is not None

    def predict(self, image_bytes: bytes) -> dict:
        """
        Détecter et lire une plaque d'immatriculation à partir de bytes.

        Décode les données binaires de l'image en matrice OpenCV, puis
        exécute le pipeline complet de reconnaissance de plaques
        (détection YOLO + lecture OCR). Si plusieurs plaques sont
        détectées, le résultat ayant le score de confiance le plus élevé
        est retourné.

        Args:
            image_bytes (bytes): Données binaires de l'image soumise
                pour la détection (formats supportés : JPEG, PNG, BMP,
                et tout format pris en charge par ``cv2.imdecode``).

        Returns:
            dict: Dictionnaire contenant les clés suivantes :
                - ``plate_text`` (str | None) : Texte de la plaque
                  d'immatriculation détectée, ou ``None`` si aucune
                  plaque n'a été reconnue.
                - ``confidence`` (float) : Score de confiance de la
                  détection, compris entre 0.0 et 1.0, arrondi à
                  4 décimales. Vaut ``0.0`` si aucune plaque n'est
                  détectée.
                - ``bounding_box`` (None) : Coordonnées de la boîte
                  englobante de la plaque. Actuellement ``None`` car
                  ``LPRPipeline`` ne retourne pas les bounding boxes
                  finales.

        Raises:
            RuntimeError: Si le pipeline n'est pas chargé en mémoire
                (``is_loaded()`` retourne ``False``).
            ValueError: Si les données binaires fournies ne peuvent pas
                être décodées en image valide par OpenCV.
        """
        if not self.is_loaded():
            raise RuntimeError("Modèle non chargé")

        # Décodage de l'image : conversion des bytes bruts en tableau
        # NumPy uint8, puis décodage en matrice BGR via OpenCV.
        nparr = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise ValueError("Image invalide")

        # Exécution du pipeline complet de reconnaissance : détection
        # YOLO des plaques puis lecture OCR des caractères.
        results = self.pipeline.run(image)

        if not results:
            return {
                "plate_text": None,
                "confidence": 0.0,
                "bounding_box": None,
            }

        # Sélection du meilleur résultat parmi les plaques détectées,
        # en se basant sur le score de confiance le plus élevé.
        best = max(results, key=lambda r: r["confidence"])

        return {
            "plate_text": best["plate"] if best["plate"] else None,
            "confidence": round(best["confidence"], 4),
            "bounding_box": None,  # LPRPipeline ne retourne pas les bbox finales
        }


# Instance singleton globale du prédicteur, partagée par l'ensemble
# de l'application FastAPI. Le chargement du modèle est déclenché
# lors de l'événement de démarrage de l'application via
# ``plate_predictor.load_model()`` dans ``app.main.startup_event``.
plate_predictor = PlatePredictor()