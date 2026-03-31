# ==============================================================================
# Dockerfile pour l'API SnapTaPlaque
# ==============================================================================
# Construit un conteneur léger basé sur Python 3.10 qui sert l'API FastAPI de
# reconnaissance de plaques d'immatriculation (YOLO ONNX + EasyOCR) via Uvicorn.
#
# Composants clés installés :
#   - Bibliothèques système requises par OpenCV (libgl1, libglib2.0-0)
#   - Dépendances Python depuis requirements.txt (FastAPI, SQLAlchemy, Alembic,
#     Ultralytics, EasyOCR, huggingface_hub, etc.)
#
# Construction & exécution :
#   docker-compose down && docker-compose up --build
#
# Port exposé : 8000
# ==============================================================================

# Image de base : variante slim de Python 3.10 pour minimiser la taille de l'image
FROM python:3.10-slim

# Arguments de build (peuvent être surchargés lors du `docker build`)
ARG HF_REPO_ID=0xnu/european-license-plate-recognition
ARG HF_MODEL_FILENAME=model.onnx
ARG HF_CONFIG_FILENAME=config.json

# Variables d'environnement pour la configuration du modèle via HuggingFace
ENV LPR_HF_REPO_ID=${HF_REPO_ID}
ENV LPR_HF_MODEL_FILENAME=${HF_MODEL_FILENAME}
ENV LPR_HF_CONFIG_FILENAME=${HF_CONFIG_FILENAME}
ENV LPR_HF_LOCAL_DIR=/models/hf

# Définition du répertoire de travail dans le conteneur
WORKDIR /app

# Installation des dépendances système requises par OpenCV (cv2) :
#   - libgl1        : Bibliothèque OpenGL nécessaire pour cv2.imread / traitement d'image
#   - libglib2.0-0  : Dépendance GLib pour le backend GTK d'OpenCV
# Le cache d'apt est ensuite supprimé pour garder l'image la plus petite possible.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Copier et installer les dépendances Python en premier
# (optimisation via le système de cache des layers de Docker ;
# les dépendances ne sont réinstallées que si requirements.txt change)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Configuration d'Ultralytics (YOLO) pour stocker son cache/config dans /tmp
# afin que le répertoire soit accessible en écriture sans droits supplémentaires.
# HF_HOME définit le répertoire de cache pour le téléchargement via HuggingFace
ENV YOLO_CONFIG_DIR=/tmp/Ultralytics
ENV HF_HOME=/models/hf-cache

# Pré-téléchargement des modèles OCR et des assets ONNX (HuggingFace) au moment du build.
RUN python - <<'PY'
import os
import easyocr
from huggingface_hub import hf_hub_download

easyocr.Reader(['en', 'de', 'fr', 'es', 'it', 'nl'], gpu=False, verbose=False)

repo_id = os.environ.get("LPR_HF_REPO_ID", "0xnu/european-license-plate-recognition")

for filename in (
    os.environ.get("LPR_HF_MODEL_FILENAME", "model.onnx"),
    os.environ.get("LPR_HF_CONFIG_FILENAME", "config.json"),
):
    hf_hub_download(
        repo_id=repo_id,
        filename=filename,
    )
PY

# Copie du code source complet de l'API dans le conteneur
COPY . .

# Démarrage de l'application FastAPI en utilisant Uvicorn et en écoutant sur
# toutes les interfaces réseau (0.0.0.0) via le port 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
