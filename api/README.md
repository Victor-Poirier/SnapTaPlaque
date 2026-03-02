# SnapTaPlaque API

**API de reconnaissance de plaques d'immatriculation** basée sur un pipeline **YOLOv8 + EasyOCR**.

![Python 3.10](https://img.shields.io/badge/Python-3.10-blue?logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.104.1-009688?logo=fastapi&logoColor=white)
![PyTorch](https://img.shields.io/badge/PyTorch-2.4.1-EE4C2C?logo=pytorch&logoColor=white)
![YOLOv8](https://img.shields.io/badge/YOLOv8-8.4.14-00FFFF?logo=ultralytics&logoColor=white)
![EasyOCR](https://img.shields.io/badge/EasyOCR-1.7.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

> Projet DevOps — Master 1 Informatique, parcours IA, Université du Mans.

---

## Table des matières

- [Présentation](#-présentation)
- [Lancement avec Docker](#-lancement-avec-docker)
- [Documentation interactive](#-documentation-interactive)
- [Endpoints de l'API](#-endpoints-de-lapi)
- [Modèle de reconnaissance](#-modèle-de-reconnaissance)
- [Base de données](#-base-de-données)

---

## Présentation

SnapTaPlaque est une API REST construite avec **FastAPI** qui permet de :

1. **Détecter** une plaque d'immatriculation sur une image uploadée grâce à un modèle YOLOv8
2. **Lire** le texte de la plaque via OCR (EasyOCR)
3. **Consulter** les informations détaillées d'un véhicule à partir de sa plaque d'immatriculation
4. **Gérer** une liste de véhicules favoris par utilisateur
5. **Historiser** toutes les prédictions avec statistiques associées
6. **Administrer** la plateforme (gestion des utilisateurs, statistiques globales)

L'API gère l'authentification via des tokens **JWT** (JSON Web Tokens). Chaque utilisateur doit s'inscrire 
puis se connecter pour obtenir un token d'accès qui sera requis sur la majorité des endpoints.

---

## Lancement avec Docker

L'API est entièrement conteneurisée et se lance en une seule commande grâce à Docker Compose.

**1. Cloner le dépôt :**
```bash
# Cloner le dépôt GitHub
git clone git@github.com:Victor-Poirier/SnapTaPlaque.git

# se déplacer dans le dossier de l'API
cd SnapTaPlaque/api
```
**2. Lancer l'ensemble des services (API + base de données) :**
```bash
# Lancer les services avec Docker Compose
docker-compose up --build
```
**3. Vérifier que l'API est opérationnelle :**
```bash
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
**4. Arrêter les services :**
```bash
docker-compose down
```

Au démarrage, l'application crée automatiquement les tables en base de données via la fonction 
create\_tables() puis charge le modèle de reconnaissance de plaques en mémoire. Un message dans 
les logs confirme le succès ou l'échec de ces opérations.

---

## Documentation interactive

Une fois l'API lancée, une documentation interactive est générée automatiquement par FastAPI 
et accessible directement depuis le navigateur :

| Interface        | URL                                |
|------------------|------------------------------------|
| **Swagger UI**   | http://localhost:8000/docs         |
| **ReDoc**        | http://localhost:8000/redoc        |
| **OpenAPI JSON** | http://localhost:8000/openapi.json |

**Swagger UI** permet de tester chaque endpoint directement depuis le navigateur : il suffit 
de cliquer sur un endpoint, de renseigner les paramètres et d'exécuter la requête. Pour les 
endpoints protégés, cliquer sur le bouton **Authorize** en haut à droite et saisir le token 
JWT obtenu via POST /auth/login.

---

## Endpoints de l'API

### Health

| Méthode | Endpoint   | Description              | Auth |
|---------|-----------|--------------------------|------|
| GET     | /health   | Vérifier l'état de l'API | ❌   |

Retourne le statut de l'API, l'état du modèle chargé en mémoire, la connexion à la base de données et la version.

---

### Authentification

| Méthode | Endpoint         | Description                            | Auth |
|---------|-----------------|----------------------------------------|------|
| POST    | /auth/register  | Inscription d'un nouvel utilisateur    | ❌   |
| POST    | /auth/login     | Connexion et obtention d'un token JWT  | ❌   |
| GET     | /auth/me        | Profil de l'utilisateur connecté       | ✅   |

**Inscription** — Envoyer un JSON avec username, email, password et full\_name. Le mot de passe 
est haché côté serveur avec bcrypt.

**Connexion** — Envoyer les identifiants en format application/x-www-form-urlencoded (username + 
password). L'API retourne un access\_token et un token\_type (bearer). Ce token doit être inclus 
dans l'en-tête Authorization de toutes les requêtes authentifiées :
```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```
**Profil** — Retourne les informations (id, username, email, is\_active, is\_admin) de l'utilisateur 
correspondant au token JWT fourni.

---

### Prédictions

| Méthode | Endpoint              | Description                                  | Auth |
|---------|-----------------------|----------------------------------------------|------|
| POST    | /predict              | Détecter et lire une plaque sur une image    | ✅   |
| GET     | /predictions/history  | Historique des détections de l'utilisateur    | ✅   |
| GET     | /predictions/stats    | Statistiques des détections de l'utilisateur | ✅   |

**POST /predict** — Envoyer une image en multipart/form-data (champ file). L'API exécute le pipeline 
YOLOv8 + EasyOCR et retourne :
- plate\_text : le texte lu sur la plaque (ex. AB-123-CD)
- confidence : score de confiance (0.0 à 1.0)
- bounding\_box : coordonnées en pixels de la zone détectée (x\_min, y\_min, x\_max, y\_max)
- model\_version : identifiant du modèle utilisé
- prediction\_id : identifiant unique de la prédiction en base de données

**GET /predictions/history** — Retourne la liste chronologique de toutes les détections effectuées 
par l'utilisateur connecté (id, plate\_text, confidence, created\_at).

**GET /predictions/stats** — Retourne les statistiques agrégées : total\_detections, successful\_reads,
failed\_reads, average\_confidence.

---

### Administration

> Réservé aux utilisateurs avec is\_admin = true.

| Méthode | Endpoint       | Description                       | Auth     |
|---------|---------------|-----------------------------------|----------|
| GET     | /admin/users  | Liste de tous les utilisateurs    | ✅ Admin |
| GET     | /admin/stats  | Statistiques globales plateforme  | ✅ Admin |

**GET /admin/users** — Retourne la liste complète des utilisateurs inscrits (id, username, email, is\_active, is\_admin).

**GET /admin/stats** — Retourne les compteurs globaux : total\_users, active\_users, admin\_users.

---

### Modèle

| Méthode | Endpoint      | Description                        | Auth |
|---------|--------------|-------------------------------------|------|
| GET     | /model/info  | Informations sur le modèle chargé  | ❌   |

Retourne les métadonnées du pipeline de reconnaissance : model\_name, algorithm et version. Exemple de réponse :
```json
{
  "model_name": "SnapTaPlaque - YOLO + EasyOCR",
  "algorithm": "YOLOv8 + EasyOCR", 
  "version": "1.0"
}
```
---

### Véhicules

| Méthode | Endpoint        | Description                                          | Auth |
|---------|----------------|------------------------------------------------------|------|
| GET     | /vehicles/info | Informations détaillées d'un véhicule par sa plaque  | ✅   |

**Paramètre query** : license\_plate (string, obligatoire) — la plaque d'immatriculation à rechercher.

Retourne les informations complètes du véhicule : license\_plate, brand, model, year, color, engine, trim. Retourne une erreur 404 si aucun véhicule ne correspond à la plaque fournie.

Exemple de réponse :
```json
{
  "license_plate": "AB-123-CD",
  "brand": "Renault",
  "model": "Clio",
  "year": 2021,
  "color": "Gris",
  "engine": "1.5 dCi",
  "trim": "Intens"
}
```
---

### Favoris

| Méthode | Endpoint           | Description                                    | Auth |
|---------|--------------------|------------------------------------------------|------|
| POST    | /favorites/add     | Ajouter un véhicule aux favoris                | ✅   |
| DELETE  | /favorites/remove  | Retirer un véhicule des favoris                | ✅   |
| GET     | /favorites/        | Liste des véhicules favoris de l'utilisateur   | ✅   |

**POST /favorites/add** — Paramètre query : license\_plate. Ajoute le véhicule correspondant aux favoris de l'utilisateur connecté. Retourne une erreur 400 si le véhicule est déjà dans les favoris.

**DELETE /favorites/remove** — Paramètre query : license\_plate. Retire le véhicule des favoris de l'utilisateur connecté.

**GET /favorites/** — Retourne la liste complète des véhicules favoris de l'utilisateur connecté, chaque entrée contenant toutes les informations du véhicule (license\_plate, brand, model, year, color, engine, trim).

---

## Modèle de reconnaissance

Le pipeline de reconnaissance de plaques d'immatriculation repose sur deux composants complémentaires :

**1. Détection — YOLOv8**

Le modèle YOLOv8 (You Only Look Once, version 8) est un réseau de neurones convolutif entraîné pour détecter la zone de la plaque d'immatriculation sur une image. Il retourne les coordonnées de la bounding box (rectangle englobant) ainsi qu'un score de confiance.

- Architecture : YOLOv8 (Ultralytics)
- Tâche : détection d'objets (classe unique : plaque d'immatriculation)
- Format du modèle : fichier .pt (PyTorch)
- Chargement : au démarrage de l'API via plate\_predictor.load\_model()

**2. Lecture — EasyOCR**

Une fois la zone de la plaque localisée par YOLO, l'image est recadrée et transmise à EasyOCR pour la reconnaissance optique de caractères. EasyOCR extrait le texte alphanumérique de la plaque.

- Bibliothèque : EasyOCR
- Langues configurées : français, anglais
- Post-traitement : nettoyage du texte (suppression des caractères parasites, mise en forme du format d'immatriculation)

Le modèle est chargé une seule fois en mémoire au démarrage du serveur. Si le chargement échoue, l'API démarre malgré tout mais les endpoints de prédiction retourneront une erreur HTTP 503.

---

## 🗄 Base de données

L'API utilise **PostgreSQL** en production (via Docker Compose) et peut fonctionner avec **SQLite** en développement local. L'ORM **SQLAlchemy** gère l'abstraction de la base de données.

Au démarrage, la fonction create\_tables() est automatiquement appelée pour créer les tables si elles n'existent pas encore (DDL auto-généré via Base.metadata.create\_all).

### Tables

**users** — Comptes utilisateurs

| Colonne           | Type    | Description                   |
|-------------------|---------|-------------------------------|
| id                | int     | Clé primaire (auto-incrémentée) |
| username          | string  | Nom d'utilisateur (unique)    |
| email             | string  | Adresse email (unique)        |
| hashed\_password  | string  | Mot de passe haché (bcrypt)   |
| full\_name        | string  | Nom complet                   |
| is\_active        | boolean | Compte actif (défaut : true)  |
| is\_admin         | boolean | Droits admin (défaut : false) |

**predictions** — Historique des détections

| Colonne      | Type     | Description                       |
|--------------|----------|-----------------------------------|
| id           | int      | Clé primaire (auto-incrémentée)   |
| plate\_text  | string   | Texte de la plaque détectée       |
| confidence   | float    | Score de confiance (0.0–1.0)      |
| created\_at  | datetime | Date et heure de la prédiction    |
| user\_id     | int      | Clé étrangère vers users          |

**vehicles** — Informations véhicules

| Colonne          | Type   | Description                       |
|------------------|--------|-----------------------------------|
| license\_plate   | string | Plaque d'immatriculation (PK)     |
| brand            | string | Marque du véhicule                |
| model            | string | Modèle du véhicule                |
| year             | int    | Année de mise en circulation      |
| color            | string | Couleur principale                |
| engine           | string | Type de motorisation              |
| trim             | string | Niveau de finition                |

**user\_favorites** — Table d'association (relation M:N entre users et vehicles)

| Colonne          | Type   | Description                       |
|------------------|--------|-----------------------------------|
| user\_id         | int    | Clé étrangère vers users          |
| license\_plate   | string | Clé étrangère vers vehicles       |

### Schéma relationnel
```bash
    ┌──────────┐       ┌─────────────────┐       ┌──────────┐
    │  users   │──1:N──│  predictions    │       │ vehicles │
    └──────────┘       └─────────────────┘       └──────────┘
         │                                            │
         └──────────M:N (user_favorites)──────────────┘
```
---

**Contact** : vincent.proudy.etu@univ-lemans.fr