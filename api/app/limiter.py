"""
limiter.py — Configuration du rate limiting pour l'API SnapTaPlaque.

Ce module centralise l'instance du Limiter slowapi afin d'éviter
les imports circulaires entre main.py et les routeurs.

Le ``Limiter`` est instancié une seule fois ici et importé par les
modules qui en ont besoin (``main.py`` pour l'enregistrement sur
l'application FastAPI, et les routeurs pour décorer les endpoints
protégés).

Architecture du rate limiting :
    - ``main.py`` importe ``limiter`` pour l'attacher à ``app.state``
      et enregistrer le gestionnaire d'exception ``RateLimitExceeded``.
    - Les routeurs (``auth.py``, ``predictions.py``, etc.) importent
      ``limiter`` pour appliquer le décorateur ``@limiter.limit()``
      sur les endpoints sensibles.

Limites appliquées dans le projet :
    - ``POST /auth/login``            — 10 requêtes/minute par IP
    - ``POST /auth/register``         — 5 requêtes/minute par IP
    - ``POST /predictions/predict``   — 5 requêtes/minute par IP

Configuration :
    - **key_func** : ``get_remote_address`` — identifie chaque client
      par son adresse IP distante extraite de l'objet ``Request``.

Exemple d'utilisation dans un routeur ::

    from app.limiter import limiter
    from fastapi import Request

    @router.get("/endpoint")
    @limiter.limit("10/minute")
    async def my_endpoint(request: Request):
        return {"status": "ok"}

Note:
    Le paramètre ``request: Request`` est **obligatoire** dans la
    signature de tout endpoint décoré par ``@limiter.limit()`` afin
    que slowapi puisse extraire l'adresse IP du client.

Version : 1.0.0

.. seealso::
    - ``app.main`` — Enregistrement du limiter sur l'application FastAPI.
    - ``app.routers.auth`` — Rate limiting sur login et register.
    - ``app.routers.predictions`` — Rate limiting sur l'endpoint predict.
    - https://github.com/laurentS/slowapi — Documentation officielle de slowapi.
"""

from slowapi import Limiter
from slowapi.util import get_remote_address

# Instance globale du Limiter slowapi, configuré pour utiliser
# l'adresse IP du client comme clé d'identification pour appliquer
# les limites de requêtes. On protège ainsi l'API contre les abus
# pour ne pas faire ralentir le service pour les autres utilisateurs.
#
# Cette instance est importée par :
#   - ``app.main``              -> ``app.state.limiter = limiter``
#   - ``app.routers.auth``      -> ``@limiter.limit("10/minute")``
#   - ``app.routers.predictions`` -> ``@limiter.limit("5/minute")``
limiter: Limiter = Limiter(key_func=get_remote_address)
