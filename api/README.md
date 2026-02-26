# SnapTaPlaque — API de Reconnaissance de Plaques d'Immatriculation
![Python 3.10](https://img.shields.io/badge/Python-3.10-blue?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.104.1-009688?logo=fastapi&logoColor=white)
![PyTorch](https://img.shields.io/badge/PyTorch-2.4.1-EE4C2C?logo=pytorch&logoColor=white)
![YOLOv8](https://img.shields.io/badge/YOLOv8-8.4.14-00FFFF?logo=ultralytics&logoColor=white)
![EasyOCR](https://img.shields.io/badge/EasyOCR-1.7.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

## Utilisation
```bash
docker-compose down && docker-compose up --build

# Accéder à l'API : http://localhost:8000
# Documentation interactive : http://localhost:8000/docs
```

## Résumé des modifications

Ce README détaille toutes les modifications apportées pour transformer l'ancienne API de **scoring crédit** en une API de **reconnaissance de plaques d'immatriculation (LPR)**.

---

## Vue d'ensemble des changements

| Composant | Avant (Crédit) | Après (LPR) |
|---|---|---|
| Endpoint `/predict` | JSON (âge, revenu, montant, durée) | Upload d'image (multipart) |
| Modèle ML | Scoring crédit (scikit-learn/flaml) | YOLOv8 + EasyOCR |
| Base de données | Champs crédit | `filename` + `results` (JSON) |
| Schémas Pydantic | CreditRequest, CreditResponse, PredictionHistory, PredictionStats | PlateHistory, PlateStats |
| Dépendances | scikit-learn, flaml, joblib | ultralytics, easyocr, torch, opencv |

---

## Fichiers modifiés

### 1. `api/app/routers/predictions.py`

#### Imports supprimés
- CreditRequest, CreditResponse, PredictionHistory, PredictionStats
- app.predictor (ancien module de scoring crédit)

#### Imports ajoutés
- UploadFile, File depuis fastapi
- cv2, numpy
- LPRPipeline depuis api.app.model.lpr\_engine
- PlateHistory, PlateStats depuis api.app.schemas

#### Endpoint POST /predict

**Avant :**
```Python
@router.post("/predict", response_model=CreditResponse)
    async def predict(request: CreditRequest, ...):
        # Recevait age, income, credit_amount, duration en JSON
        # Utilisait predictor.predict() pour le scoring crédit
```
**Après :**
```Python
    @router.post("/predict")
    async def predict_plate(file: UploadFile, ...):
        # Reçoit une image uploadée
        # Décode l'image avec OpenCV
        # Exécute LPRPipeline.run(image)
        # Sauvegarde filename + results JSON en base
```
#### Endpoints GET /history et GET /stats

- `/history` : response\_model changé de List[PredictionHistory] à List[PlateHistory]
- `/stats` : response\_model changé de PredictionStats à PlateStats

---

### 2. `api/app/crud.py`

#### Fonction create\_prediction

**Avant :**
```Python
    def create_prediction(
        db, user_id, age, income, credit_amount,
        duration, decision, probability, model_version, ip_address
    ) -> Prediction:
```
**Après :**
```Python
    def create_prediction(
        db: Session,
        user_id: int,
        filename: str,
        results: dict,
    ) -> Prediction:
```
#### Fonction get\_user\_prediction\_stats

**Avant :** Calculait total\_predictions, approved, rejected, approval\_rate.

**Après :** Retourne uniquement total\_predictions.
```Python
    def get_user_prediction_stats(db: Session, user_id: int):
        total = db.query(Prediction).filter(Prediction.user_id == user_id).count()
        return {"total_predictions": total}
```
---

### 3. `api/app/database.py`

#### Modèle Prediction

**Colonnes supprimées :**
- age (Integer)
- income (Float)
- credit\_amount (Float)
- duration (Integer)
- decision (String)
- probability (Float)
- model\_version (String)
- ip\_address (String)

**Colonnes ajoutées :**
- filename (String, nullable=False)
- results (JSON, nullable=False)
- created\_at (DateTime, default=datetime.utcnow)
```Python
    class Prediction(Base):
        __tablename__ = "predictions"

        id = Column(Integer, primary_key=True, index=True)
        user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
        filename = Column(String, nullable=False)
        results = Column(JSON, nullable=False)
        created_at = Column(DateTime, default=datetime.utcnow)
```
---

### 4. `api/app/schemas.py`

#### Schémas supprimés
- CreditRequest — validation des données crédit (age, income, credit\_amount, duration)
- CreditResponse — réponse avec decision, probability, model_ver
- PredictionHistory — historique avec champs crédit
- PredictionStats — statistiques avec approved/rejected/approval_rate

#### Schémas ajoutés
```Python
    class PlateHistory(BaseModel):
        id: int
        plate_text: Optional[str] = None
        confidence: Optional[float] = None
        created_at: datetime

        class Config:
            from_attributes = True

    class PlateStats(BaseModel):
        total_predictions: int
```
#### Schémas conservés (inchangés)
- LoginRequest
- Token
- UserCreate
- UserResponse

---

### 5. `requirements.txt`

#### Dépendances supprimées
- scikit-learn==1.3.2
- flaml==2.1.1
- joblib==1.3.2
- numpy==1.26.2
- pandas==2.1.3

#### Dépendances ajoutées

| Package | Version | Utilité |
|---|---|---|
| numpy | 1.23.5 | Manipulation de tableaux |
| opencv-python-headless | 4.11.0.86 | Traitement d'images (sans GUI) |
| ultralytics | 8.4.14 | YOLOv8 — détection de plaques |
| easyocr | 1.7.0 | OCR — lecture des caractères |
| torch | 2.4.1 | Backend deep learning |
| torchvision | 0.19.1 | Utilitaires vision PyTorch |
| pillow | 9.5.0 | Manipulation d'images |
| pyyaml | 6.0.3 | Parsing de config YAML |
| scipy | 1.15.3 | Dépendance scikit-image |
| scikit-image | 0.24.0 | Dépendance easyocr |
| shapely | 2.1.2 | Dépendance easyocr |
| pyclipper | 1.4.0 | Dépendance easyocr |
| pandas | 2.0.3 | Manipulation de données |
| requests | 2.32.5 | Requêtes HTTP |

#### Dépendances conservées
- fastapi==0.104.1
- uvicorn[standard]==0.24.0
- pydantic==2.5.0
- pydantic-settings==2.1.0
- sqlalchemy==2.0.23
- psycopg2-binary==2.9.9
- alembic==1.13.0
- python-jose[cryptography]==3.3.0
- passlib[bcrypt]==1.7.4
- python-multipart==0.0.6
- bcrypt==4.1.2
- python-dotenv==1.0.0
- email-validator>=2.0.0

---

### 6. Nouveau module ajouté : `api/app/model/lpr_engine.py`

Contient la classe LPRPipeline qui encapsule :
1. **Détection** — YOLOv8 pour localiser les plaques dans l'image
2. **OCR** — EasyOCR pour lire les caractères sur les plaques détectées

---

## Flux de fonctionnement
```Text
    Client                API                     LPRPipeline
      |                    |                          |
      |-- POST /predict -->|                          |
      |   (image upload)   |                          |
      |                    |-- cv2.imdecode() ------->|
      |                    |                          |
      |                    |-- pipeline.run(image) -->|
      |                    |                   YOLO detect + EasyOCR
      |                    |<-- results (JSON) -------|
      |                    |                          |
      |                    |-- create_prediction() -->|  DB
      |                    |   (filename, results)    |
      |                    |                          |
      |<-- JSON response --|                          |
      |  {filename, results, prediction_id}           |
```

--- 


## Fichiers modifiés hors `api/app/`

### 7. `Dockerfile`

**Modification :** Version Python alignée sur l'environnement conda.

| Élément | Avant  | Après  |
|---|---|---|
| Image de base | `python:3.11-slim` | `python:3.10-slim` |

Le reste du fichier (WORKDIR, COPY, RUN pip install, CMD uvicorn) est inchangé.

---

### 8. `docker-compose.yml`

**Modification :** Noms de la base de données, de l'utilisateur et du mot de passe PostgreSQL mis à jour.

| Variable d'environnement | Avant  | Après  |
|---|---|---|
| `POSTGRES_DB` | `credit_scoring_db` | `snaptaplaque_db` |
| `POSTGRES_USER` | `credit_user` | `plate_user` |
| `POSTGRES_PASSWORD` | `credit_password` | `plate_password` |
| `DATABASE_URL` | `...credit_user:credit_password.../credit_scoring_db` | `...plate_user:plate_password.../snaptaplaque_db` |

---

### 9. `init-db.sql`

**Modification :** Références à la base et à l'utilisateur mises à jour.

| Élément | Avant  | Après  |
|---|---|---|
| Base de données | `credit_scoring_db` | `snaptaplaque_db` |
| Utilisateur | `credit_user` | `plate_user` |

```sql
GRANT ALL PRIVILEGES ON DATABASE snaptaplaque_db TO plate_user;
```

---

### 10. `openapi.yaml`

Réécriture complète pour refléter l'API SnapTaPlaque.

#### Métadonnées

| Champ | Avant  | Après  |
|---|---|---|
| `title` | API Credit Scoring - DevOps M1 IA | API SnapTaPlaque - Reconnaissance de Plaques d'Immatriculation |
| `description` | API de credit scoring basée sur AutoML | API de reconnaissance de plaques basée sur YOLO + EasyOCR |
| Serveur SwaggerHub | Présent | Supprimé |

#### Endpoint `/predict`

| Aspect | Avant  | Après  |
|---|---|---|
| Content-Type | `application/json` | `multipart/form-data` |
| Input schema | `CreditRequest` (age, income, credit\_amount, duration) | `PlateDetectionRequest` (file: binary) |
| Output schema | `CreditResponse` (decision, probability) | `PlateDetectionResponse` (plate\_text, confidence, bounding\_box) |

#### Schemas supprimés

- `CreditRequest`
- `CreditResponse`
- `PredictionHistory`
- `PredictionStats`

#### Schemas ajoutés

- `PlateDetectionRequest` — Upload d'image (binary)
- `PlateDetectionResponse` — plate\_text, confidence, bounding\_box, model\_version, prediction\_id
- `DetectionHistory` — id, plate\_text, confidence, created\_at
- `DetectionStats` — total\_detections, successful\_reads, failed\_reads, average\_confidence

#### Schemas conservés (avec exemples mis à jour)

- `UserCreate`, `UserResponse`, `Token`, `UserStats`, `ModelInfo`, `HealthResponse`, `auth_login_body`

---

### 11. `setup.sh`

Remplacement des anciennes variables Credit Scoring par les valeurs SnapTaPlaque.

| Variable | Avant  | Après  |
|---|---|---|
| `DB_NAME` | `credit_scoring_db` | `snaptaplaque_db` |
| `DB_USER` | `credit_user` | `plate_user` |
| `DB_PASSWORD` | `credit_password` | `plate_password` |

Le champ `full_name` a été supprimé de la création du compte admin (non présent dans le schéma `UserCreate`).

#### Fichiers supprimés

| Fichier | Raison |
|---|---|
| `setup_db.sh` | Redondant avec `setup.sh` (sous-ensemble) |
| `setup_v1.0.sh` | Doublon de `setup.sh` sans sudo |

---

### 12. `requirements.txt`

Voir section 5 ci-dessus pour le détail complet des dépendances ajoutées, supprimées et conservées.

---

### 13. `.env` / `.env.example`

**Modification :** Variables d'environnement de connexion à la base mises à jour.

| Variable | Avant  | Après  |
|---|---|---|
| `DATABASE_URL` | `postgresql://credit_user:credit_password@localhost/credit_scoring_db` | `postgresql://plate_user:plate_password@localhost/snaptaplaque_db` |

---

## Récapitulatif global des fichiers modifiés hors `api/app/`

| Fichier | Type de modification |
|---|---|
| `Dockerfile` | Version Python 3.11 → 3.10 |
| `docker-compose.yml` | Noms DB/user/password |
| `init-db.sql` | Noms DB/user |
| `openapi.yaml` | Réécriture complète (schemas + endpoints) |
| `setup.sh` | Variables DB + suppression full\_name |
| `setup_db.sh` | **Supprimé** |
| `setup_v1.0.sh` | **Supprimé** |
| `requirements.txt` | Dépendances ML remplacées |
| `.env` / `.env.example` | DATABASE\_URL mise à jour 
