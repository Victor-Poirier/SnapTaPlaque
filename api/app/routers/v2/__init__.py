"""
routers.v2 — Sous-package des routeurs FastAPI v2 de l'API SnapTaPlaque.

Ce sous-package regroupe les modules définissant les routeurs FastAPI
(``APIRouter``) de la **version 2** de l'API. Seuls les modules dont
les endpoints ont évolué par rapport à la version 1 sont redéfinis ici ;
les modules inchangés sont réutilisés directement depuis ``app.routers.v1``
et montés sous le préfixe ``/v2/`` dans ``app/main.py``.

Tous les endpoints de cette version sont montés sous le préfixe ``/v2/``
par le point d'entrée principal de l'application (``app/main.py``).

Principes de versionnage appliqués :
    - **Ne versionner que ce qui change** : seuls les modules impactés
      par une évolution fonctionnelle ou technique (changement de modèle
      IA, modification du schéma de réponse, etc.) sont dupliqués dans
      ce sous-package. Les endpoints stables sont réutilisés depuis V1.

    - **Rétrocompatibilité** : les champs de réponse existants ne sont
      jamais supprimés, seuls de nouveaux champs sont ajoutés (ex.
      ``model_version``), ce qui permet aux clients existants de migrer
      progressivement.

    - **Dépréciation** : lorsque la V2 est déclarée stable, les endpoints
      V1 correspondants reçoivent les en-têtes ``Deprecation: true`` et
      ``Sunset: <date>`` afin de signaler leur retrait programmé.

Note:
    Pour ajouter un nouveau module V2 (par exemple ``vehicles.py`` avec
    un enrichissement des données véhicule), créer le fichier dans ce
    répertoire et remplacer le montage du routeur V1 par le routeur V2
    dans ``app/main.py`` sous le préfixe ``/v2/vehicles``.

.. seealso::
    - ``app.main`` — Point d'entrée de l'application FastAPI, où les
      routeurs de chaque version sont montés.
    - ``app.routers.v1`` — Version 1 des routeurs, base de référence.
    - ``app.limiter`` — Instance du rate limiter partagée par les
      routeurs protégés des deux versions.

Version : 2.0.0
"""