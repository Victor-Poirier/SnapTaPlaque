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
    """
    Configuration du moteur LPR HuggingFace.
    
    Cette classe regroupe l'ensemble des paramètres de configuration statiques
    utilisés par le pipeline de reconnaissance de plaques d'immatriculation.
    Les valeurs sont initialisées par défaut ou peuvent être surchargées via 
    des variables d'environnement.
    """

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
    """
    Pipeline de reconnaissance de plaques (LPR) combinant YOLOv12 et EasyOCR.
    
    Cette classe gère le téléchargement des modèles depuis HuggingFace, l'initialisation
    des moteurs de détection (YOLO) et de lecture (EasyOCR), ainsi que le traitement 
    des images pour en extraire le texte des plaques d'immatriculation.
    """

    def __init__(self):
        """
        Initialise YOLO ONNX + EasyOCR avec fallback offline.
        
        Ce constructeur télécharge les poids et la configuration du modèle YOLO 
        depuis le HuggingFace Hub s'ils ne sont pas déjà en cache. Il instancie ensuite
        les objets YOLO et EasyOCR. Si activé dans la configuration, un système de 
        préchauffage (warmup) est lancé.
        """
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
        """
        Lance une passe à blanc pour compiler les kernels ONNX avant la 1ere requête.
        
        Permet d'éviter un pic de latence lors de la première utilisation de l'inférence.
        En cas d'échec du préchauffage, l'erreur est volontairement ignorée pour ne pas 
        bloquer l'exécution globale.
        """
        try:
            size = CFG.warmup_img_size
            dummy = np.zeros((size, size, 3), dtype=np.uint8)
            _ = self.plate_model(dummy, conf=CFG.plate_conf, verbose=False)
        except Exception:
            # Le warmup ne doit pas bloquer l'API en cas d'echec
            pass

    def extract_roi(self, image: np.ndarray, bbox: list) -> np.ndarray:
        """
        Extrait une région d'intérêt (ROI) de l'image en bornant les coordonnées.

        :param image: L'image source depuis laquelle extraire la région.
        :type image: numpy.ndarray
        :param bbox: Une liste contenant les coordonnées de la boîte englobante 
                     au format [x_min, y_min, x_max, y_max].
        :type bbox: list
        :return: L'image recadrée correspondante à la région d'intérêt.
        :rtype: numpy.ndarray
        """
        x_min, y_min, x_max, y_max = map(int, bbox)

        h, w = image.shape[:2]
        x_min = max(0, min(x_min, w))
        x_max = max(0, min(x_max, w))
        y_min = max(0, min(y_min, h))
        y_max = max(0, min(y_max, h))

        return image[y_min:y_max, x_min:x_max]

    def extract_ocr(self, roi_img: np.ndarray) -> tuple:
        """
        Extrait le texte de la plaque d'immatriculation via EasyOCR sur une zone recadrée.

        Traite l'image par conversion en niveaux de gris, amélioration du contraste 
        (CLAHE), redimensionnement si nécessaire et ajout de bordures, avant d'appliquer 
        la reconnaissance optique de caractères.

        :param roi_img: Image recadrée (Région d'Intérêt) représentant la plaque.
        :type roi_img: numpy.ndarray
        :return: Un tuple contenant le texte reconnu et le score de confiance maximum.
                 Retourne ("", 0.0) si l'image est vide ou si aucune reconnaissance 
                 n'atteint le seuil de confiance minimal.
        :rtype: tuple (str, float)
        """
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
        Exécute la détection des plaques puis l'OCR sur une image complète.

        Cette méthode prend une image en entrée, lance la détection avec le modèle YOLO, 
        extrait chaque zone détectée avec une légère marge de tolérance, et y applique l'OCR.

        :param image_np: L'image complète sur laquelle rechercher les plaques, au format BGR.
        :type image_np: numpy.ndarray
        :return: Une liste de dictionnaires, chaque dictionnaire contenant le texte 
                 de la plaque (clé "plate") et sa confiance (clé "confidence").
        :rtype: list
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

