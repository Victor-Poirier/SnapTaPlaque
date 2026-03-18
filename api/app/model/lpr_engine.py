"""
Pipeline LPR base sur YOLOv12 ONNX (HuggingFace) + EasyOCR.

Le moteur detecte directement les plaques dans l'image complete,
puis applique OCR sur chaque zone detectee.
"""

import os
import warnings

import cv2
import numpy as np
import easyocr
from ultralytics import YOLO
from huggingface_hub import hf_hub_download


class CFG:
    """Configuration du moteur LPR HuggingFace."""

    hf_repo_id = os.getenv("LPR_HF_REPO_ID", "0xnu/european-license-plate-recognition")
    hf_model_filename = os.getenv("LPR_HF_MODEL_FILENAME", "model.onnx")
    hf_config_filename = os.getenv("LPR_HF_CONFIG_FILENAME", "config.json")
    # hf_local_dir n'est plus utilisé avec la méthode hf_hub_download standard

    # Seuil de confiance detection; aligne sur l'exemple utilisateur (0.5) pour la fiabilite
    plate_conf = float(os.getenv("LPR_PLATE_CONF", 0.5))

    # EasyOCR — langues de l'exemple utilisateur: en, de, fr, es, it, nl
    ocr_conf = float(os.getenv("LPR_OCR_CONF", 0.2))
    ocr_languages = [lang for lang in os.getenv("LPR_OCR_LANGS", "en,de,fr,es,it,nl").split(",") if lang]
    ocr_allowlist = os.getenv("LPR_OCR_ALLOWLIST", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")

    # Warmup pour eviter le spike de latence sur la 1ere requete
    warmup_enabled = os.getenv("LPR_WARMUP", "1") == "1"
    warmup_img_size = int(os.getenv("LPR_WARMUP_SIZE", 640))


class LPRPipeline:
    def __init__(self):
        """Initialise YOLO ONNX + EasyOCR avec fallback offline."""
        warnings.filterwarnings("ignore")

        # Téléchargement via HuggingFace Hub (utilise le cache si disponible)
        # Cela correspond à l'usage standard demandé par l'utilisateur
        print(f"Loading YOLO model from HF: {CFG.hf_repo_id}/{CFG.hf_model_filename}")
        model_path = hf_hub_download(
            repo_id=CFG.hf_repo_id,
            filename=CFG.hf_model_filename
        )
        # On s'assure aussi que la config est présente (optionnel mais recommandé)
        hf_hub_download(
            repo_id=CFG.hf_repo_id,
            filename=CFG.hf_config_filename
        )

        self.plate_model = YOLO(model_path, task="detect")
        self.reader = easyocr.Reader(
            CFG.ocr_languages,
            gpu=False,
            verbose=False,
        )
        print(f"EasyOCR initialized with languages: {CFG.ocr_languages}")

        if CFG.warmup_enabled:
            self._warmup()

    def _warmup(self) -> None:
        """Lance une passe a blanc pour compiler les kernels ONNX avant la 1ere requete."""
        try:
            size = CFG.warmup_img_size
            dummy = np.zeros((size, size, 3), dtype=np.uint8)
            _ = self.plate_model(dummy, conf=CFG.plate_conf, verbose=False)
        except Exception:
            # Le warmup ne doit pas bloquer l'API en cas d'echec
            pass

    def extract_roi(self, image: np.ndarray, bbox: list) -> np.ndarray:
        """Extrait une ROI en bornant les coordonnees a l'image."""
        x_min, y_min, x_max, y_max = map(int, bbox)

        h, w = image.shape[:2]
        x_min = max(0, min(x_min, w))
        x_max = max(0, min(x_max, w))
        y_min = max(0, min(y_min, h))
        y_max = max(0, min(y_max, h))

        return image[y_min:y_max, x_min:x_max]

    def extract_ocr(self, roi_img: np.ndarray) -> tuple:
        """Retourne (texte_plaque, confiance_max) pour un crop de plaque."""
        if roi_img.size == 0:
            return "", 0.0

        # 1. Conversion N&B
        gray = cv2.cvtColor(roi_img, cv2.COLOR_RGB2GRAY)

        # 2. Amelioration contraste (CLAHE) - aide pour les reflets et ombres
        # Le Clip Limit permet de limiter l'amplification du bruit
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        gray = clahe.apply(gray)

        # 3. Upscaling si trop petit (hauteur < 64px)
        if gray.shape[0] < 64:
            scale = 64 / gray.shape[0]
            gray = cv2.resize(gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

        # 4. Ajout de bordures (padding) pour eviter que les caracteres touchent le bord
        # EasyOCR performe mieux avec un peu d'espace autour du texte
        gray = cv2.copyMakeBorder(gray, 10, 10, 10, 10, cv2.BORDER_CONSTANT, value=[255, 255, 255])

        results = self.reader.readtext(
            gray,
            allowlist=CFG.ocr_allowlist,
        )

        text_plate = ""
        max_conf = 0.0

        for _, text, conf in results:
            if conf > CFG.ocr_conf:
                text_plate += text
                max_conf = max(max_conf, conf)

        return text_plate.upper().replace(" ", ""), float(max_conf)

    def run(self, image_np: np.ndarray) -> list:
        """
        Execute la detection et retourne une liste de:
        {"plate": <str>, "confidence": <float>}.
        """
        plates_found = []

        image_rgb = cv2.cvtColor(image_np, cv2.COLOR_BGR2RGB)
        detection_results = self.plate_model(image_rgb, conf=CFG.plate_conf, verbose=False)

        for result in detection_results:
            boxes = result.boxes
            if boxes is None:
                continue

            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy()

                # Expansion de la bbox de 5% pour ne pas couper les caracteres sur les bords
                # Cela aide grandement l'OCR en donnant du contexte (padding naturel)
                w_box = x2 - x1
                h_box = y2 - y1
                margin_x = w_box * 0.05
                margin_y = h_box * 0.05

                x1 = x1 - margin_x
                x2 = x2 + margin_x
                y1 = y1 - margin_y
                y2 = y2 + margin_y

                plate_img = self.extract_roi(image_rgb, [x1, y1, x2, y2])
                text, conf = self.extract_ocr(plate_img)
                if text:
                    plates_found.append(
                        {
                            "plate": text,
                            "confidence": float(conf),
                        }
                    )

        return plates_found

