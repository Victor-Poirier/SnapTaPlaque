"""
app — Package racine de l'API SnapTaPlaque.

Ce package constitue le point d'entrée du module Python ``app`` et
regroupe l'ensemble des sous-modules composant l'API REST de
reconnaissance de plaques d'immatriculation SnapTaPlaque.

La présence de ce fichier ``__init__.py`` permet à Python de
reconnaître le répertoire ``app/`` comme un package importable,
autorisant les imports absolus et relatifs entre les sous-modules
(ex. ``from app.predictor import plate_predictor``).

Structure du package :
    - ``main.py``          — Point d'entrée FastAPI, configuration de
      l'application, enregistrement des routeurs et gestion des
      événements de cycle de vie (démarrage / arrêt).
    - ``database.py``      — Configuration de la connexion à la base
      de données SQLAlchemy et création des tables.
    - ``models.py``        — Définition des modèles ORM SQLAlchemy
      (tables ``users``, ``predictions``).
    - ``schemas.py``       — Schémas Pydantic de validation des
      requêtes et de sérialisation des réponses.
    - ``crud.py``          — Fonctions CRUD pour les opérations sur
      les utilisateurs et les prédictions.
    - ``security.py``      — Utilitaires de hachage et de vérification
      de mots de passe (bcrypt via Passlib).
    - ``predictor.py``     — Wrapper du pipeline de reconnaissance de
      plaques pour l'intégration avec FastAPI.
    - ``routers/``         — Sous-package contenant les routeurs
      FastAPI (auth, predictions, admin, model).
    - ``model/``           — Sous-package contenant le pipeline LPR
      (détection YOLO + lecture OCR).

Version : 1.0.0
"""