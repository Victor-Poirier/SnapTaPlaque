"""
routers — Package racine des routeurs FastAPI de l'API SnapTaPlaque.

Ce package organise l'ensemble des endpoints REST de l'API selon un
système de **versionnage par préfixe d'URL** (``/v1/``, ``/v2/``, etc.).
Chaque version de l'API est isolée dans un sous-package dédié, ce qui
permet de faire évoluer les endpoints (changement de modèle IA, modification
du schéma de réponse, ajout de fonctionnalités) sans impacter les
consommateurs existants.

Architecture du versionnage :
    ::

        routers/
        ├── __init__.py          ← ce fichier (package racine)
        ├── v1/                  ← Version 1 — pipeline YOLOv8 + EasyOCR
        │   ├── __init__.py
        │   ├── auth.py          ← Inscription, connexion, profil JWT
        │   ├── predictions.py   ← Détection et reconnaissance de plaques
        │   ├── admin.py         ← Administration (liste utilisateurs, stats)
        │   ├── model.py         ← Informations sur le modèle LPR
        │   ├── vehicles.py      ← Consultation des véhicules par plaque
        │   └── favorites.py     ← Gestion des véhicules favoris
        └── v2/                  ← Version 2 — nouveau pipeline IA
            ├── __init__.py
            └── nouveau_endpoint.py ← exemple d'endpoint modifié pour V2

Principes de versionnage appliqués :
    - **Ne versionner que ce qui change** : seuls les modules impactés
      par une évolution fonctionnelle ou technique sont redéfinis dans
      le sous-package de la nouvelle version. Les modules inchangés sont
      réutilisés directement depuis la version précédente et montés sous
      le nouveau préfixe dans ``app/main.py``.

    - **Rétrocompatibilité** : les champs de réponse existants ne sont
      jamais supprimés d'une version à l'autre. Seuls de nouveaux champs
      peuvent être ajoutés (ex. ``model_version`` en V2), ce qui permet
      aux clients existants de migrer progressivement.

    - **Coexistence des versions** : toutes les versions déclarées sont
      servies simultanément par la même instance de l'application. Un
      client peut continuer à appeler ``/v1/predictions/predict`` pendant
      que d'autres utilisent ``/v2/predictions/predict`` par exemple.

    - **Dépréciation progressive** : lorsqu'une nouvelle version est
      déclarée stable, les endpoints de la version précédente reçoivent
      les en-têtes HTTP ``Deprecation: true`` et ``Sunset: <date>`` afin
      de signaler leur retrait programmé aux consommateurs.

    - **Découverte** : un endpoint ``GET /versions`` exposé à la racine
      de l'application permet aux clients de lister les versions
      disponibles, leur statut (stable, beta, deprecated) et le modèle
      IA associé.

Cycle de vie d'une version :
    1. **beta**       — Nouvelle version déployée, ouverte aux early
       adopters. Peut encore subir des modifications mineures.
    2. **stable**     — Version validée en production, contrat d'API figé.
    3. **deprecated** — Version marquée pour retrait futur. Les en-têtes
       ``Deprecation`` et ``Sunset`` sont ajoutés aux réponses.
    4. **retired**    — Version retirée, les endpoints retournent
       HTTP 410 Gone.

Ajout d'une nouvelle version :
    1. Créer le sous-package ``routers/vN/`` avec son ``__init__.py``.
    2. N'y placer que les modules dont les endpoints évoluent.
    3. Dans ``app/main.py``, monter les nouveaux routeurs sous le préfixe
       ``/vN/`` et réutiliser les routeurs de la version précédente pour
       les endpoints inchangés.
    4. Mettre à jour l'endpoint ``GET /versions`` pour déclarer la
       nouvelle version.

.. seealso::
    - ``app.routers.v1`` — Version 1 des routeurs (pipeline YOLOv8 + EasyOCR).
    - ``app.routers.v2`` — Version 2 des routeurs (nouveau pipeline IA).
    - ``app.main`` — Point d'entrée FastAPI où les routeurs de chaque
      version sont montés avec leurs préfixes respectifs.
    - ``app.limiter`` — Instance du rate limiter partagée par les
      routeurs protégés de toutes les versions.

Version : 1.0.0
"""
