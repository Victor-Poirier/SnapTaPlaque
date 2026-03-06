"""
routers.v1 — Sous-package des routeurs FastAPI v1 de l'API SnapTaPlaque.

Ce sous-package regroupe les modules définissant les routeurs FastAPI
(``APIRouter``) de la **version 1** de l'API. Chaque routeur encapsule
un ensemble cohérent d'endpoints REST organisés par domaine fonctionnel.

Tous les endpoints de cette version sont montés sous le préfixe ``/v1/``
par le point d'entrée principal de l'application (``app/main.py``).

Modules :
    - ``auth.py``          — Endpoints d'authentification (inscription,
      connexion, consultation du profil via token JWT).
    - ``predictions.py``   — Endpoints de soumission d'images et de
      consultation des résultats de reconnaissance de plaques
      (pipeline YOLOv8 + EasyOCR).
    - ``admin.py``         — Endpoints d'administration réservés aux
      utilisateurs avec le rôle administrateur (liste des utilisateurs,
      statistiques globales de la plateforme).
    - ``model.py``         — Endpoint d'information sur le modèle de
      reconnaissance de plaques (état de chargement, type de pipeline).
    - ``vehicles.py``      — Endpoint de consultation des informations
      d'un véhicule à partir de sa plaque d'immatriculation.
    - ``favorites.py``     — Endpoints de gestion des véhicules favoris
      de l'utilisateur connecté (ajout, suppression, liste).

Préfixes d'URL (définis dans ``app/main.py``) :
    - ``/v1/auth``         → ``auth.router``
    - ``/v1/predictions``  → ``predictions.router``
    - ``/v1/admin``        → ``admin.router``
    - ``/v1/model``        → ``model.router``
    - ``/v1/vehicles``     → ``vehicles.router``
    - ``/v1/favorites``    → ``favorites.router``

Note:
    Lors de l'introduction d'une version ultérieure (``v2``), seuls les
    modules dont les endpoints changent (par exemple ``predictions.py``
    avec un nouveau modèle IA) nécessitent une réécriture. Les modules
    inchangés peuvent être réutilisés directement sous le nouveau préfixe
    ``/v2/`` dans ``main.py``.

.. seealso::
    - ``app.main`` — Point d'entrée de l'application FastAPI, où les
      routeurs de chaque version sont montés.
    - ``app.routers.v2`` — Version 2 des routeurs (lorsqu'elle existe).
    - ``app.limiter`` — Instance du rate limiter partagée par les
      routeurs protégés (``auth.py``, ``predictions.py``).

Version : 1.0.0
"""