"""
dependencies.py — Dépendances d'injection FastAPI pour l'API SnapTaPlaque.

Ce module définit les dépendances FastAPI réutilisables permettant
d'extraire et de valider l'utilisateur courant à partir du contexte
de la requête. Ces dépendances sont injectées dans les endpoints via
le mécanisme ``Depends`` de FastAPI pour centraliser la logique
d'autorisation.

Composants exposés :
    - ``get_current_user``       — Dépendance FastAPI extrayant
      l'utilisateur courant depuis la base de données à partir de son
      nom d'utilisateur et vérifiant que son compte est actif.
    - ``get_current_admin_user`` — Dépendance FastAPI vérifiant que
      l'utilisateur courant possède les privilèges d'administration.

Version : 1.0.0
"""

# app/dependencies.py
from fastapi import Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.crud import get_user_by_username
from app.database import User


def get_current_user(db: Session = Depends(get_db), username: str = "admin") -> User:
    """
    Extraire l'utilisateur courant et vérifier que son compte est actif.

    Dépendance FastAPI qui recherche l'utilisateur en base de données
    à partir du nom d'utilisateur fourni, puis vérifie que le compte
    existe et est actif (``is_active=True``). Si l'utilisateur n'est
    pas trouvé ou si son compte est désactivé, une erreur HTTP 401
    est levée.

    Note:
        Le paramètre ``username`` possède une valeur par défaut
        ``"admin"`` à des fins de développement. En production, cette
        valeur doit être extraite du token d'authentification ou d'un
        autre mécanisme d'identification sécurisé.

    Args:
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        username (str): Nom d'utilisateur à rechercher en base de
            données. Par défaut ``"admin"``.

    Returns:
        User: Instance ORM de l'utilisateur authentifié et actif.

    Raises:
        HTTPException (401): Si aucun utilisateur ne correspond au nom
            fourni ou si le compte de l'utilisateur est désactivé
            (``is_active=False``).
    """
    user = get_user_by_username(db, username=username)
    if not user or not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Inactive user")
    return user


def get_current_admin_user(current_user: User = Depends(get_current_user)) -> User:
    """
    Vérifier que l'utilisateur courant possède les privilèges d'administration.

    Dépendance FastAPI qui s'appuie sur ``get_current_user`` pour
    obtenir l'utilisateur authentifié et actif, puis vérifie qu'il
    dispose du rôle administrateur (``is_admin=True``). Si l'utilisateur
    n'est pas administrateur, une erreur HTTP 403 est levée.

    Cette dépendance est destinée aux endpoints d'administration
    nécessitant des privilèges élevés (gestion des utilisateurs,
    consultation des statistiques globales, etc.).

    Args:
        current_user (User): Utilisateur authentifié et actif, injecté
            automatiquement par la dépendance ``get_current_user``.

    Returns:
        User: Instance ORM de l'utilisateur authentifié, actif et
            disposant des privilèges d'administration.

    Raises:
        HTTPException (403): Si l'utilisateur ne possède pas le rôle
            administrateur (``is_admin=False``).
    """
    if not current_user.is_admin:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not an admin")
    return current_user