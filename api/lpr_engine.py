"""
Offre une implémentation d'un pipeline de reconnaissance de plaques d'immatriculation (LPR) utilisant les
modèles YOLO pour la détection de véhicules et de plaques, ainsi que EasyOCR pour l'extraction du texte des plaques.
Le pipeline est conçu avec une approche en deux étapes (détection de véhicules puis détection de
plaques) et un fallback pour détecter les plaques directement sur l'image si aucune n'est trouvée dans les véhicules
détectés. Les résultats incluent les plaques trouvées et leurs confiances.

Date de création : 25 février 2026
Version : 1.0



================ Exemple de code pour l'API ================
    pipeline = LPRPipeline()

    @app.post("/predict")
    async def predict_plate(file: UploadFile = File(...)):
        # Lecture de l'image envoyée
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise HTTPException(status_code=400, detail="Invalid image file")

        # Exécution de la pipeline
        results = pipeline.run(image)

        return {
            "filename": file.filename,
            "results": results
        }
===========================================================
"""

import cv2
import numpy as np
import pandas as pd
import easyocr
from ultralytics import YOLO
import torch


class CFG:
    """
    Configuration pour le pipeline de reconnaissance de plaques d'immatriculation (LPR).

    Explication des paramètres :
        weights: chemin vers les poids du modèle de détection de véhicules (YOLO).
        plate_weights: chemin vers les poids du modèle de détection de plaques (YOLO).
        vehicles_class: liste des classes de véhicules à détecter (car, motorcycle, bus, truck).
        vehicle_conf: seuil de confiance pour la détection de véhicules.
        plate_conf: seuil de confiance pour la détection de plaques.
        ocr_conf: seuil de confiance pour l'extraction de texte via OCR.
    """
    weights = 'vehicule_model.pt'
    plate_weights = 'plate_model.pt'
    vehicles_class = [2, 3, 5, 7]
    vehicle_conf = 0.5
    plate_conf = 0.3
    ocr_conf = 0.1


class LPRPipeline:
    def __init__(self):
        """
        Initialise le pipeline de reconnaissance de plaques d'immatriculation en chargeant les modèles et en configurant les paramètres nécessaires.
        """
        self.device = 'cuda' if torch.cuda.is_available() else 'cpu'
        # Chargement des modèles
        self.vehicle_model = YOLO(CFG.weights).to(self.device)
        self.plate_model = YOLO(CFG.plate_weights).to(self.device)
        self.reader = easyocr.Reader(['en'], gpu=(self.device != 'cpu'))

        # Mapping des classes
        self.dict_classes = {i: self.vehicle_model.model.names[i] for i in CFG.vehicles_class}

    def extract_roi(self, image : np.ndarray, bbox : list) -> np.ndarray:
        """
        Extrait la région d'intérêt (ROI) de l'image en fonction de la bounding box.

        Args:
            image: l'image d'entrée
            bbox: la bounding box au format [x_min, y_min, x_max, y_max]

        Returns:
            Le sous-image correspondant à la bounding box.
        """
        x_min, y_min, x_max, y_max = map(int, bbox)
        return image[y_min:y_max, x_min:x_max]

    def extract_ocr(self, roi_img : np.ndarray) -> tuple:
        """
        Effectue l'OCR sur la région d'intérêt (ROI) pour extraire le texte de la plaque d'immatriculation.

        Args:
            roi_img: L'image de la plaque d'immatriculation extraite

        Returns:
            Le texte extrait de la plaque d'immatriculation et la confiance maximale associée.
        """

        # Les caractères possibles pour les plaques d'immatriculation sont limités à des chiffres et des lettres majuscules
        results = self.reader.readtext(np.asarray(roi_img), allowlist='0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ')
        text_plate = ""
        max_conf = 0

        for (bbox, text, conf) in results:
            if conf > CFG.ocr_conf:
                text_plate += text
                max_conf = max(max_conf, conf)

        return text_plate.upper(), max_conf

    def run(self, image_np : np.ndarray) -> list:
        """
        Execute le pipeline de reconnaissance de plaques d'immatriculation sur une image donnée.

        Args:
            image_np: L'image d'entrée prise depuis un téléphone portable.

        Returns:
            Le résultat de la reconnaissance de plaques d'immatriculation, incluant les plaques trouvées et leurs confiances.
        """

        # Détection des véhicules dans l'image
        vehicle_results = self.vehicle_model.predict(
            image_np, conf=CFG.vehicle_conf, classes=CFG.vehicles_class, verbose=False
        )

        df_vehicles = pd.DataFrame(
            vehicle_results[0].cpu().numpy().boxes.data,
            columns=['xmin', 'ymin', 'xmax', 'ymax', 'conf', 'class']
        )

        plates_found = []

        if not df_vehicles.empty:
            for _, v_row in df_vehicles.iterrows():
                vehicle_img = self.extract_roi(image_np, [v_row['xmin'], v_row['ymin'], v_row['xmax'], v_row['ymax']])

                # Détection de plaque dans le véhicule
                plate_results = self.plate_model.predict(vehicle_img, conf=CFG.plate_conf, verbose=False)
                df_p = pd.DataFrame(plate_results[0].cpu().numpy().boxes.data,
                                    columns=['xmin', 'ymin', 'xmax', 'ymax', 'conf', 'class'])

                if not df_p.empty:
                    # On prend la meilleure plaque si plusieurs sont détectées
                    best_p = df_p.loc[df_p['conf'].idxmax()]
                    plate_img = self.extract_roi(vehicle_img,
                                                 [best_p['xmin'], best_p['ymin'], best_p['xmax'], best_p['ymax']])

                    # OCR pour extraire le texte de la plaque
                    text, conf = self.extract_ocr(plate_img)
                    plates_found.append({"plate": text, "confidence": float(conf)})

        # Fallback = recherche sur toute l'image si rien n'est trouvé
        if not plates_found:
            plate_results = self.plate_model.predict(image_np, conf=CFG.plate_conf, verbose=False)
            for result in plate_results[0].boxes.data:
                p_bbox = result[:4].cpu().numpy()
                plate_img = self.extract_roi(image_np, p_bbox)
                text, conf = self.extract_ocr(plate_img)
                if text:
                    plates_found.append({"plate": text, "confidence": float(conf)})

        return plates_found