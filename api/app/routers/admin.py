"""
admin.py — Routeur d'administration pour l'API SnapTaPlaque.

Ce module définit les endpoints réservés aux utilisateurs disposant du
rôle administrateur (``is_admin=True``). Tous les endpoints de ce routeur
sont protégés par la dépendance ``get_current_admin_user`` qui vérifie
à la fois la validité du token JWT et le statut administrateur de
l'utilisateur authentifié.

Endpoints exposés :
    - ``GET /users``  — Liste complète de tous les utilisateurs inscrits.
    - ``GET /stats``  — Statistiques globales de la plateforme (nombre
      d'utilisateurs total, actifs, administrateurs).

Ces endpoints sont montés sous le préfixe ``/admin`` par le routeur
principal de l'application (voir ``app/main.py``).

Dépendances :
    - ``app.database.get_db`` — Fournit une session SQLAlchemy liée à la
      base PostgreSQL, automatiquement fermée en fin de requête.
    - ``app.dependencies.get_current_admin_user`` — Valide le token JWT
      et vérifie que l'utilisateur possède les privilèges administrateur.
    - ``app.crud.get_all_users`` — Requête CRUD retournant la liste de
      tous les comptes utilisateurs.
    - ``app.crud.get_global_stats`` — Requête CRUD retournant les
      statistiques agrégées de la plateforme.
    - ``app.schemas.UserResponse`` — Schéma Pydantic de sérialisation
      d'un utilisateur (exclut les champs sensibles comme le mot de passe).

Version : 1.0.0
"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import get_current_admin_user
from app.crud import get_all_users, get_global_stats
from app.schemas import UserResponse

# Instance du routeur FastAPI pour les endpoints d'administration.
# Ce routeur est ensuite inclus dans l'application principale avec
# le préfixe "/admin" et le tag "Admin" pour la documentation OpenAPI.
router = APIRouter()


@router.get("/users", response_model=list[UserResponse])
def list_users(
    db: Session = Depends(get_db),
    admin=Depends(get_current_admin_user),
):
    """
    Récupérer la liste de tous les utilisateurs inscrits.

    Endpoint réservé aux administrateurs. Retourne l'ensemble des comptes
    utilisateurs enregistrés dans la base de données, sérialisés selon
    le schéma ``UserResponse`` (les champs sensibles tels que
    ``hashed_password`` sont exclus de la réponse).

    Args:
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        admin: Utilisateur administrateur authentifié, injecté par
            ``get_current_admin_user``. Déclenche une erreur HTTP 401/403
            si l'utilisateur n'est pas connecté ou n'est pas administrateur.

    Returns:
        list[UserResponse]: Liste de tous les utilisateurs avec leurs
            informations publiques (id, username, email, is_active, is_admin).
    """
    return get_all_users(db)


@router.get("/stats")
def global_stats(
    db: Session = Depends(get_db),
    admin=Depends(get_current_admin_user),
):
    """
    Récupérer les statistiques globales de la plateforme.

    Endpoint réservé aux administrateurs. Retourne des métriques agrégées
    sur l'ensemble de la base utilisateurs : nombre total d'utilisateurs,
    nombre d'utilisateurs actifs et nombre d'administrateurs.

    Ces statistiques sont utiles pour les tableaux de bord de supervision
    et le suivi opérationnel de la plateforme.

    Args:
        db (Session): Session SQLAlchemy injectée automatiquement par
            la dépendance ``get_db``.
        admin: Utilisateur administrateur authentifié, injecté par
            ``get_current_admin_user``. Déclenche une erreur HTTP 401/403
            si l'utilisateur n'est pas connecté ou n'est pas administrateur.

    Returns:
        dict: Dictionnaire contenant les statistiques globales, typiquement
            ``total_users``, ``active_users`` et ``admin_users``.
    """
    return get_global_stats(db)
