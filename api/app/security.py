"""
security.py — Utilitaires de hachage et de vérification de mots de passe pour l'API SnapTaPlaque.

Ce module centralise les opérations cryptographiques liées à la gestion
des mots de passe utilisateurs. Il fournit des fonctions de hachage et
de vérification basées sur l'algorithme bcrypt, garantissant un stockage
sécurisé des mots de passe en base de données.

Composants exposés :
    - ``pwd_context``         — Instance ``CryptContext`` de Passlib
      configurée avec l'algorithme bcrypt, utilisée en interne par les
      fonctions de hachage et de vérification.
    - ``get_password_hash``   — Fonction générant un hash bcrypt à
      partir d'un mot de passe en clair.
    - ``verify_password``     — Fonction vérifiant qu'un mot de passe
      en clair correspond à un hash bcrypt stocké en base de données.

Version : 1.0.0
"""

from passlib.context import CryptContext

# Instance globale du contexte cryptographique Passlib, configurée
# avec l'algorithme bcrypt comme schéma principal de hachage. Le
# paramètre ``deprecated="auto"`` permet à Passlib de marquer
# automatiquement les anciens schémas comme obsolètes et de
# re-hacher les mots de passe lors de la prochaine vérification
# si un schéma plus récent est configuré.
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def get_password_hash(password: str) -> str:
    """
    Générer un hash bcrypt à partir d'un mot de passe en clair.

    Produit un hash sécurisé du mot de passe fourni en utilisant
    l'algorithme bcrypt avec un sel aléatoire généré automatiquement.
    Le hash résultant est destiné à être stocké en base de données
    en remplacement du mot de passe en clair.

    Args:
        password (str): Mot de passe en clair à hacher.

    Returns:
        str: Hash bcrypt du mot de passe, incluant le sel et le
            facteur de coût (format ``$2b$...``).
    """
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Vérifier qu'un mot de passe en clair correspond à un hash bcrypt.

    Compare le mot de passe en clair fourni par l'utilisateur lors de
    la connexion avec le hash bcrypt stocké en base de données. La
    vérification est effectuée en temps constant pour prévenir les
    attaques par canal auxiliaire (timing attacks).

    Args:
        plain_password (str): Mot de passe en clair soumis par
            l'utilisateur lors de la tentative d'authentification.
        hashed_password (str): Hash bcrypt du mot de passe stocké en
            base de données (format ``$2b$...``).

    Returns:
        bool: ``True`` si le mot de passe en clair correspond au hash,
            ``False`` sinon.
    """
    return pwd_context.verify(plain_password, hashed_password)
