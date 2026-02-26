"""
routers — Sous-package des routeurs FastAPI de l'API SnapTaPlaque.

Ce sous-package regroupe les modules définissant les routeurs FastAPI
(``APIRouter``) de l'application. Chaque routeur encapsule un ensemble
cohérent d'endpoints REST organisés par domaine fonctionnel.

Modules :
    - ``auth.py``          — Endpoints d'authentification (inscription,
      connexion, gestion des tokens JWT).
    - ``predictions.py``   — Endpoints de soumission d'images et de
      consultation des résultats de reconnaissance de plaques.
    - ``admin.py``         — Endpoints d'administration (gestion des
      utilisateurs, statistiques globales).
    - ``model.py``         — Endpoints d'information sur le modèle de
      reconnaissance de plaques (version, algorithme).

Version : 1.0.0
"""