# SnapTaPlaque

<Img src="./Android/app/src/main/res/drawable/logo.png" alt="SnapTaPlaque Logo" width="200" align="center" />


Projet DevOps — Master 1 Informatique, parcours IA, Le Mans Université  
Module 178UD10 — Bossard Guilian · Perron Nathan · Poirier Victor · Proudy Vincent

![Android](https://img.shields.io/badge/Android-Java-3DDC84?logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-Swift-000000?logo=apple&logoColor=white)
![FastAPI](https://img.shields.io/badge/API-FastAPI-009688?logo=fastapi&logoColor=white)
![YOLOv12](https://img.shields.io/badge/ML-YOLOv12%20%2B%20EasyOCR-00FFFF)
![PostgreSQL](https://img.shields.io/badge/DB-PostgreSQL%2016-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Infra-Docker%20Compose-2496ED?logo=docker&logoColor=white)

Application Android de **reconnaissance automatique de plaques d'immatriculation**, connectée à une API Python de machine learning exposant un pipeline **YOLOv12 + EasyOCR**.

---

## Table des matières

- [SnapTaPlaque](#snaptaplaque)
  - [Table des matières](#table-des-matières)
  - [Présentation](#présentation)
  - [Architecture globale](#architecture-globale)
  - [Application Android](#application-android)
    - [Prérequis (Android)](#prérequis-android)
  - [Application iOS](#application-ios)
    - [Prérequis (iOS)](#prérequis-ios)
  - [Installation](#installation)
  - [API Python](#api-python)
    - [Stack technique](#stack-technique)
    - [Endpoints principaux](#endpoints-principaux)
  - [Lancement rapide](#lancement-rapide)
    - [API (Docker Compose)](#api-docker-compose)
    - [Arrêt des services](#arrêt-des-services)
  - [Documentation](#documentation)
  - [RGPD \& Sécurité](#rgpd--sécurité)
  - [Équipe](#équipe)

---

## Présentation

SnapTaPlaque permet à tout utilisateur de **scanner, saisir ou dicter** une plaque d'immatriculation pour obtenir instantanément les informations détaillées du véhicule associé.

**Fonctionnalités principales :**

- Détection automatique d'une plaque depuis une photo (YOLOv8 + EasyOCR)
- Saisie manuelle (clavier, tactile, roulette)
- Reconnaissance vocale de la plaque
- Consultation des informations véhicule (marque, modèle, motorisation, énergie, informations complémentaires)
- Gestion de véhicules favoris par utilisateur
- Affichage de la localisation de l'utilisateur
- Historique des recherches
- Profil utilisateur avec authentification JWT

---

## Architecture globale

```
┌─────────────────────────────────────┐        ┌──────────────────────────────────────┐
│        Application Android          │        │           API Python (FastAPI)       │
│                                     │        │                                      │
│  ┌─────────┐ ┌──────────┐ ┌──────┐  │  HTTP  │  ┌──────────────┐  ┌─────────────┐   │
│  │ Scanner │ │Historique│ │Profil│  │◄──────►│  │  YOLOv12     │  │  PostgreSQL │   │
│  │ plaque  │ │          │ │      │  │  REST  │  │  + EasyOCR   │  │  (Docker)   │   │
│  └─────────┘ └──────────┘ └──────┘  │        │  └──────────────┘  └─────────────┘   │
│          Architecture MVC           │        │       JWT · Rate limiting · RGPD     │
└─────────────────────────────────────┘        └──────────────────────────────────────┘
```

Le projet est découpé en deux dépôts :

| Dépôt | Technologie | Description |
|---|---|---|
| [`SnapTaPlaque-Android`](./Android/) | Java / Android SDK | Application mobile Android |*
| [`SnapTaPlaque-IOS`](./iOS/) | Swift / UIKit | Application mobile iOS (projet secondaire) |
| [`SnapTaPlaque-API`](./api) | Python / FastAPI | Backend ML et gestion des données |

---

## Application Android

Développée en **Java** selon le patron **MVC**, l'application propose trois onglets principaux (inspirés de Clash Royale / TikTok) :

| Onglet | Description |
|---|---|
| **Centre** (accueil) | Recherche d'un véhicule — photo, roulette, voix (+ clavier) |
| ◀️ **Gauche** | Historique des recherches (du plus au moins récent) |
| ▶️ **Droite** | Profil utilisateur — infos personnelles, photo, véhicules favoris |

### Prérequis (Android)

- Android Studio Hedgehog ou supérieur
- Android SDK 26+
- Connexion à l'API (locale ou déployée)

## Application iOS

Développée en **Swift** avec **UIKit**, l'application iOS offre une expérience utilisateur fluide et intuitive, avec les mêmes fonctionnalités que la version Android. Néanmoins, elle ne reprend pas forcément toute la DA de l'application Android. C'est un projet secondaire qui a été développé en parallèle, mais qui n'est pas le focus principal de ce projet DevOps. Elle permet aux utilisateurs iOS de pouvoir essayer l'application SnapTaPlaque.

### Prérequis (iOS)

- Xcode
- iOS 26+
- Connexeion à l'API (locale ou déployée)

## Installation

```bash
# Cloner le dépôt
git clone https://github.com/<org>/SnapTaPlaque-Android.git
cd SnapTaPlaque-Android

# Ouvrir dans Android Studio et synchroniser Gradle
# Pour lancer l'API localement, suivre les instructions dans la section "API (Docker Compose)"
```

---

## API Python

L'API REST est construite avec **FastAPI** et conteneurisée via **Docker Compose**.  
Elle expose un pipeline de reconnaissance basé sur **YOLOv8** (détection) + **EasyOCR** (lecture OCR).

### Stack technique

| Composant | Technologie |
|---|---|
| Framework | FastAPI 0.104.1 |
| ML — Détection | YOLOv12 |
| ML — OCR | EasyOCR 1.7.0 |
| Runtime ML | PyTorch 2.4.1 |
| Base de données | PostgreSQL 16 (prod) / SQLite (dev) |
| ORM | SQLAlchemy + Alembic |
| Auth | JWT (python-jose) + bcrypt |
| Rate limiting | slowapi |
| Conteneurisation | Docker Compose |

### Endpoints principaux

| Groupe | Méthode | Endpoint | Auth |
|---|---|---|---|
| Health | GET | `/health` | ❌ |
| Auth | POST | `/v1/account/register` | ❌ |
| Auth | POST | `/v1/account/login` | ❌ |
| Prédiction | POST | `/v1/predictions/predict` | ✅ |
| Prédiction | GET | `/v1/predictions/history` | ✅ |
| Véhicules | GET | `/v1/vehicles/info` | ✅ |
| Véhicules | GET | `/v1/vehicles/history` | ✅ |
| Favoris | POST/DELETE/GET | `/v1/favorites/*` | ✅ |
| Admin | GET | `/v1/admin/users` | ✅ Admin |

> Documentation interactive complète : `http://localhost:8000/docs`

## Lancement rapide

### API (Docker Compose)

```bash
git clone https://github.com/<org>/SnapTaPlaque-API.git
cd SnapTaPlaque-API/api

# Lancer l'API + PostgreSQL
docker compose up --build

# Vérifier que l'API est opérationnelle
curl http://localhost:8000/health
```

Réponse attendue :
```json
{
  "status": "healthy",
  "model_loaded": true,
  "database": "connected",
  "version": "1.0"
}
```

### Arrêt des services

```bash
docker compose down
```

---

## Documentation

| Ressource | URL |
|---|---|
| Swagger UI | `http://localhost:8000/docs` |
| ReDoc | `http://localhost:8000/redoc` |
| OpenAPI JSON | `http://localhost:8000/openapi.json` |
| Politique de confidentialité | `GET /privacy-policy` |

---

## RGPD & Sécurité

L'API est conçue dans le respect du **RGPD (UE 2016/679)** :

- Consentement explicite requis à l'inscription (`gdpr_consent`)
- Droit d'accès : `GET /v1/account/me/data-export`
- Droit à l'effacement : `DELETE /v1/account/me/delete-account`
- Mots de passe hachés avec **bcrypt**, authentification par **JWT signé**
- Les images soumises pour prédiction **ne sont pas conservées** après traitement
- Rate limiting sur les endpoints sensibles (5–10 req/min)

---

## Équipe

| Nom | Rôle |
|---|---|
| Bossard Guilian | Développeur |
| Perron Nathan | Développeur |
| Poirier Victor | Développeur |
| Proudy Vincent | Développeur |

> Master 1 Informatique — Parcours Intelligence Artificielle  
> Le Mans Université — Institut d'Informatique Claude Chappe
