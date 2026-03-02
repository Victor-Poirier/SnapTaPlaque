"""
auth.py — Module d'authentification et de sécurité pour l'API SnapTaPlaque.

Ce module centralise l'ensemble de la logique d'authentification de
l'application : hachage et vérification des mots de passe, création et
décodage des tokens JWT, ainsi que les dépendances FastAPI permettant
d'extraire et de valider l'utilisateur courant à partir d'une requête
authentifiée.

Composants exposés :
    - ``get_password_hash``       — Hache un mot de passe en clair avec bcrypt.
    - ``verify_password``         — Compare un mot de passe en clair avec
      son hash bcrypt.
    - ``create_access_token``     — Génère un token JWT signé avec une
      durée de vie configurable.
    - ``decode_access_token``     — Décode et valide un token JWT, levant
      une erreur HTTP 401 si le token est invalide ou expiré.
    - ``get_current_user``        — Dépendance FastAPI extrayant l'utilisateur
      authentifié à partir du token Bearer.
    - ``get_current_active_user`` — Dépendance FastAPI vérifiant que
      l'utilisateur authentifié possède un compte actif.

Configuration :
    - ``SECRET_KEY``                  — Clé secrète utilisée pour signer et
      vérifier les tokens JWT, chargée depuis ``app.config.settings``.
    - ``ALGORITHM``                   — Algorithme de signature JWT (HS256).
    - ``ACCESS_TOKEN_EXPIRE_MINUTES`` — Durée de validité par défaut des
      tokens (60 minutes).

Version : 1.0.0
"""

from datetime import datetime, timedelta
from typing import Optional

from jose import jwt, JWTError
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from passlib.context import CryptContext

from app.database import get_db


from app import crud, models, schemas, database
from app.config import settings

# ================== CONFIG ==================

# Clé secrète utilisée pour signer les tokens JWT. Cette valeur est
# chargée depuis les paramètres de l'application (variable d'environnement
# ou fichier .env). Elle ne doit jamais être exposée publiquement.
SECRET_KEY = settings.SECRET_KEY

# Algorithme de signature utilisé pour l'encodage et le décodage des
# tokens JWT. HS256 (HMAC-SHA256) est un algorithme symétrique : la même
# clé sert à signer et à vérifier.
ALGORITHM = "HS256"

# Durée de validité par défaut des tokens d'accès, en minutes.
# Passé ce délai, le token est considéré comme expiré et l'utilisateur
# doit se ré-authentifier.
ACCESS_TOKEN_EXPIRE_MINUTES = 60

# Schéma OAuth2 « Bearer token » utilisé par FastAPI pour extraire
# automatiquement le token JWT depuis l'en-tête ``Authorization``.
# Le paramètre ``tokenUrl`` indique l'endpoint de connexion pour la
# documentation interactive Swagger UI.
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

# Contexte de hachage Passlib configuré avec l'algorithme bcrypt.
# L'option ``deprecated="auto"`` permet de migrer automatiquement les
# anciens schémas de hachage lors de la prochaine vérification.
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# ================== PASSWORD ==================
def get_password_hash(password: str) -> str:
    """
    Hacher un mot de passe en clair avec bcrypt.

    Utilise le contexte Passlib configuré pour produire un hash bcrypt
    sécurisé, incluant un sel aléatoire généré automatiquement.

    Args:
        password (str): Mot de passe en clair à hacher.

    Returns:
        str: Hash bcrypt du mot de passe, prêt à être stocké en base
            de données.
    """
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    """
    Vérifier qu'un mot de passe en clair correspond à un hash bcrypt.

    Compare le mot de passe fourni par l'utilisateur avec le hash stocké
    en base de données. La comparaison est effectuée en temps constant
    pour prévenir les attaques par analyse temporelle (timing attacks).

    Args:
        plain (str): Mot de passe en clair soumis par l'utilisateur.
        hashed (str): Hash bcrypt stocké en base de données.

    Returns:
        bool: ``True`` si le mot de passe correspond au hash,
            ``False`` sinon.
    """
    return pwd_context.verify(plain, hashed)

# ================== JWT ==================
def create_access_token(
    data: dict,
    expires_delta: Optional[timedelta] = None
) -> str:
    """
    Générer un token JWT signé.

    Crée un token d'accès contenant les données fournies (typiquement
    ``{"sub": username}``) et une date d'expiration. Le token est signé
    avec la clé secrète de l'application en utilisant l'algorithme HS256.

    Args:
        data (dict): Dictionnaire des claims à inclure dans le payload
            du token. Doit contenir au minimum la clé ``"sub"`` avec
            le nom d'utilisateur.
        expires_delta (Optional[timedelta]): Durée de validité du token.
            Si ``None``, une durée par défaut de 15 minutes est appliquée.

    Returns:
        str: Token JWT encodé sous forme de chaîne de caractères,
            prêt à être transmis au client.
    """
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


def decode_access_token(token: str) -> dict:
    """
    Décoder et valider un token JWT.

    Vérifie la signature du token et sa date d'expiration. Si le token
    est valide, retourne le payload décodé sous forme de dictionnaire.
    En cas d'échec (signature invalide, token expiré, format incorrect),
    une exception HTTP 401 est levée.

    Args:
        token (str): Token JWT encodé à décoder.

    Returns:
        dict: Payload décodé du token contenant les claims (``sub``,
            ``exp``, etc.).

    Raises:
        HTTPException (401): Si le token est invalide, expiré ou si
            sa signature ne peut pas être vérifiée.
    """
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalide ou expiré",
        )

# ================== DEPENDANCES ==================
def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> database.User:
    """
    Extraire l'utilisateur courant à partir du token JWT.

    Dépendance FastAPI qui décode le token Bearer présent dans l'en-tête
    ``Authorization`` de la requête, extrait le nom d'utilisateur du
    claim ``sub``, puis charge l'utilisateur correspondant depuis la
    base de données.

    Cette dépendance est utilisée en amont de ``get_current_active_user``
    et peut être injectée directement dans les endpoints nécessitant
    une authentification sans vérification du statut actif.

    Args:
        token (str): Token JWT extrait automatiquement de l'en-tête
            ``Authorization: Bearer <token>`` par le schéma OAuth2.
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.

    Returns:
        database.User: Instance ORM de l'utilisateur authentifié.

    Raises:
        HTTPException (401): Si le token est invalide, si le claim
            ``sub`` est absent ou si aucun utilisateur correspondant
            n'est trouvé en base de données.
    """
    payload = decode_access_token(token)
    username: str | None = payload.get("sub")

    if username is None:
        raise HTTPException(status_code=401, detail="Token invalide")

    user = crud.get_user_by_username(db, username)
    if not user:
        raise HTTPException(status_code=401, detail="Utilisateur non trouvé")

    return user


def get_current_active_user(
    current_user: database.User = Depends(get_current_user),
) -> database.User:
    """
    Vérifier que l'utilisateur authentifié possède un compte actif.

    Dépendance FastAPI qui s'appuie sur ``get_current_user`` pour obtenir
    l'utilisateur courant, puis vérifie que son compte n'a pas été
    désactivé (``is_active=True``). Si le compte est désactivé, une
    erreur HTTP 403 est levée.

    Cette dépendance est la plus couramment utilisée dans les endpoints
    protégés de l'application pour garantir à la fois l'authentification
    et l'activation du compte.

    Args:
        current_user (database.User): Utilisateur authentifié, injecté
            automatiquement par la dépendance ``get_current_user``.

    Returns:
        database.User: Instance ORM de l'utilisateur authentifié et actif.

    Raises:
        HTTPException (403): Si le compte de l'utilisateur est désactivé
            (``is_active=False``).
    """
    if not current_user.is_active:
        raise HTTPException(status_code=403, detail="Compte désactivé")
    return current_user
