"""
Wrapper autour de LPRPipeline pour l'intégration avec l'API FastAPI.
"""

import cv2
import numpy as np
from app.model.lpr_engine import LPRPipeline


class PlatePredictor:
    def __init__(self):
        self.pipeline = None

    def load_model(self) -> bool:
        """Charge les modèles YOLO + EasyOCR via LPRPipeline."""
        try:
            self.pipeline = LPRPipeline()
            return True
        except Exception as e:
            print(f"Erreur chargement modèle: {e}")
            self.pipeline = None
            return False

    def is_loaded(self) -> bool:
        return self.pipeline is not None

    def predict(self, image_bytes: bytes) -> dict:
        """Détecte et lit une plaque d'immatriculation à partir de bytes."""
        if not self.is_loaded():
            raise RuntimeError("Modèle non chargé")

        # Décodage de l'image
        nparr = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise ValueError("Image invalide")

        # Exécution du pipeline
        results = self.pipeline.run(image)

        if not results:
            return {
                "plate_text": None,
                "confidence": 0.0,
                "bounding_box": None,
            }

        # Meilleur résultat
        best = max(results, key=lambda r: r["confidence"])

        return {
            "plate_text": best["plate"] if best["plate"] else None,
            "confidence": round(best["confidence"], 4),
            "bounding_box": None,  # LPRPipeline ne retourne pas les bbox finales
        }


# Instance globale
plate_predictor = PlatePredictor()