"""
Pipeline LPR base sur YOLO ONNX (HuggingFace) + EasyOCR.

Le moteur detecte directement les plaques dans l'image complete,
puis applique OCR sur chaque zone detectee.
"""

from pathlib import Path
import warnings

import cv2
import numpy as np
import easyocr
from ultralytics import YOLO


class CFG:
    """Configuration du moteur LPR HuggingFace."""

    hf_repo_id = "0xnu/european-license-plate-recognition"
    hf_model_filename = "model.onnx"
    hf_config_filename = "config.json"
    hf_local_dir = "/models/hf"

    plate_conf = 0.5
    ocr_conf = 0.1
    ocr_languages = ["en"]
    ocr_allowlist = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"


class LPRPipeline:
    def __init__(self):
        """Initialise YOLO ONNX + EasyOCR avec fallback offline."""
        warnings.filterwarnings("ignore")

        model_path = self._resolve_model_file(CFG.hf_model_filename)
        # Recupere aussi config.json pour prechauffer le cache HF.
        self._resolve_model_file(CFG.hf_config_filename)

        self.plate_model = YOLO(model_path, task="detect")
        self.reader = easyocr.Reader(
            CFG.ocr_languages,
            gpu=False,
            verbose=False,
        )

    def _resolve_model_file(self, filename: str) -> str:
        """
        Resout un artefact modele strictement en local.

        En mode "zero telechargement runtime", les fichiers doivent
        avoir ete precharges pendant le build Docker.
        """
        local_dir = Path(CFG.hf_local_dir)
        local_dir.mkdir(parents=True, exist_ok=True)
        local_file_path = local_dir / filename

        if local_file_path.exists():
            return str(local_file_path)

        raise RuntimeError(
            f"Artefact modele absent: {local_file_path}. "
            "Rebuild l'image Docker pour precharger le modele."
        )

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

        # Pre-traitement: conversion N&B + upscale si petite taille
        gray = cv2.cvtColor(roi_img, cv2.COLOR_RGB2GRAY)
        if gray.shape[0] < 64:
            scale = 64 / gray.shape[0]
            gray = cv2.resize(gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

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

