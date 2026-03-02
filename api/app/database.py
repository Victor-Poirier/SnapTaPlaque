"""
database.py — Configuration de la base de données et modèles ORM de l'API SnapTaPlaque.

Ce module centralise la configuration de la connexion à la base de
données PostgreSQL via SQLAlchemy, définit les modèles ORM représentant
les tables de l'application, et expose les utilitaires nécessaires à la
gestion des sessions et à la création du schéma.

Composants exposés :
    - ``engine``          — Moteur SQLAlchemy connecté à la base de
      données PostgreSQL configurée dans ``app.config.settings``.
    - ``SessionLocal``    — Fabrique de sessions SQLAlchemy liée au
      moteur, utilisée pour créer des sessions transactionnelles.
    - ``Base``            — Classe de base déclarative SQLAlchemy dont
      héritent tous les modèles ORM de l'application.
    - ``User``            — Modèle ORM représentant un utilisateur
      enregistré sur la plateforme.
    - ``Prediction``      — Modèle ORM représentant une prédiction de
      reconnaissance de plaque associée à un utilisateur.
    - ``create_tables``   — Fonction utilitaire créant l'ensemble des
      tables définies dans les modèles ORM.
    - ``get_db``          — Générateur de dépendance FastAPI fournissant
      une session SQLAlchemy avec fermeture automatique.

Relations entre modèles :
    - Un ``User`` possède zéro ou plusieurs ``Prediction`` (relation
      one-to-many). La relation est bidirectionnelle via les attributs
      ``User.predictions`` et ``Prediction.user``.

Index :
    - ``idx_user_created`` — Index composite sur les colonnes
      ``(user_id, created_at)`` de la table ``predictions``, optimisant
      les requêtes de récupération paginée des prédictions d'un
      utilisateur triées par date.

Version : 1.0.0
"""

from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Boolean, ForeignKey, Index, JSON, Table
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime
from app.config import settings

# Moteur SQLAlchemy connecté à la base de données PostgreSQL. Le
# paramètre ``echo`` active la journalisation des requêtes SQL en mode
# debug, et ``pool_pre_ping`` vérifie la validité des connexions avant
# leur réutilisation pour éviter les erreurs de connexion périmée.
engine = create_engine(
    settings.DATABASE_URL,
    echo=settings.DEBUG,
    pool_pre_ping=True,
)

# Fabrique de sessions SQLAlchemy. Les sessions créées par cette
# fabrique ne valident pas automatiquement les transactions
# (``autocommit=False``) et ne synchronisent pas automatiquement
# les objets en mémoire avec la base (``autoflush=False``), laissant
# un contrôle explicite au développeur.
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Classe de base déclarative dont héritent tous les modèles ORM de
# l'application. Elle fournit les métadonnées nécessaires à SQLAlchemy
# pour mapper les classes Python aux tables de la base de données.
Base = declarative_base()

# Table d'association many-to-many entre users et vehicles (favoris)
user_favorites = Table(
    "user_favorites",
    Base.metadata,
    Column("user_id", Integer, ForeignKey("users.id"), primary_key=True),
    Column("license_plate", String, ForeignKey("vehicles.license_plate"), primary_key=True),
)

class User(Base):
    """
    Modèle ORM représentant un utilisateur de la plateforme SnapTaPlaque.

    Chaque utilisateur dispose d'un identifiant unique, d'une adresse
    email et d'un nom d'utilisateur uniques, d'un mot de passe haché
    (bcrypt), ainsi que de métadonnées de profil et de statut.

    Attributes:
        id (int): Identifiant unique auto-incrémenté (clé primaire).
        email (str): Adresse email unique de l'utilisateur. Indexée
            pour accélérer les recherches par email.
        username (str): Nom d'utilisateur unique. Indexé pour accélérer
            les recherches par nom d'utilisateur.
        hashed_password (str): Hash bcrypt du mot de passe de
            l'utilisateur. Ne doit jamais être exposé en réponse API.
        full_name (str): Nom complet de l'utilisateur (prénom et nom).
        is_active (bool): Indique si le compte est actif. Un compte
            désactivé ne peut pas s'authentifier. Par défaut ``True``.
        is_admin (bool): Indique si l'utilisateur possède les privilèges
            d'administration. Par défaut ``False``.
        created_at (datetime): Date et heure de création du compte.
            Définie automatiquement à l'instant de l'insertion.
        predictions (list[Prediction]): Liste des prédictions associées
            à cet utilisateur (relation one-to-many).
        favorites (list[Vehicle]): Liste des véhicules favoris de
            cet utilisateur (relation many-to-many).
    """

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    is_admin = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relation bidirectionnelle one-to-many vers le modèle Prediction.
    # L'attribut ``back_populates`` assure la synchronisation automatique
    # entre les deux côtés de la relation.
    predictions = relationship("Prediction", back_populates="user")

    # Relation many-to-many vers le modèle Vehicle via une table d'association "favorites". Un utilisateur peut avoir
    # plusieurs véhicules favoris, et un véhicule peut être favori de plusieurs utilisateurs.
    favorites = relationship("Vehicle", secondary=user_favorites, back_populates="favorited_by")


class Prediction(Base):
    """
    Modèle ORM représentant une prédiction de reconnaissance de plaque.

    Chaque prédiction est associée à un utilisateur et contient le nom
    du fichier image soumis ainsi que les résultats de la reconnaissance
    (plaques détectées, scores de confiance, coordonnées des boîtes
    englobantes) stockés au format JSON.

    Attributes:
        id (int): Identifiant unique auto-incrémenté (clé primaire).
        user_id (int): Identifiant de l'utilisateur ayant soumis la
            prédiction (clé étrangère vers ``users.id``).
        filename (str): Nom du fichier image soumis pour la détection
            de plaque d'immatriculation.
        results (dict): Dictionnaire JSON contenant les résultats de
            la reconnaissance (plaques détectées, scores de confiance,
            coordonnées des boîtes englobantes, texte OCR, etc.).
        created_at (datetime): Date et heure de création de la
            prédiction. Définie automatiquement à l'instant de
            l'insertion.
        user (User): Instance ORM de l'utilisateur propriétaire de
            cette prédiction (relation many-to-one).
    """

    __tablename__ = "predictions"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    filename = Column(String, nullable=False)
    results = Column(JSON, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relation bidirectionnelle many-to-one vers le modèle User.
    # L'attribut ``back_populates`` assure la synchronisation automatique
    # entre les deux côtés de la relation.
    user = relationship("User", back_populates="predictions")

    # Index composite sur les colonnes ``user_id`` et ``created_at``,
    # optimisant les requêtes de récupération paginée des prédictions
    # d'un utilisateur triées chronologiquement.
    __table_args__ = (
        Index("idx_user_created", "user_id", "created_at"),
    )

class Vehicle(Base):
    """
    Modèle ORM représentant un véhicule enregistré sur la plateforme SnapTaPlaque.

    Chaque véhicule est identifié de manière unique par sa plaque
    d'immatriculation et contient les informations techniques et
    descriptives associées (marque, modèle, année, couleur, motorisation
    et finition).

    Ce modèle est lié au modèle ``User`` via une relation many-to-many
    à travers la table d'association ``user_favorites``, permettant à
    plusieurs utilisateurs de marquer un même véhicule comme favori.

    Attributes:
        license_plate (str): Plaque d'immatriculation du véhicule
            (clé primaire). Indexée pour accélérer les recherches.
        brand (str): Marque du véhicule (ex. : Renault, Peugeot, BMW).
        model (str): Modèle du véhicule (ex. : Clio, 308, Série 3).
        year (int): Année de mise en circulation du véhicule.
        color (str): Couleur principale du véhicule.
        engine (str): Type de motorisation du véhicule (ex. : 1.5 dCi,
            2.0 TDI, électrique).
        trim (str): Niveau de finition du véhicule (ex. : Intens,
            Allure, Sport).
        favorited_by (list[User]): Liste des utilisateurs ayant ajouté
            ce véhicule à leurs favoris (relation many-to-many via la
            table ``user_favorites``).
    """

    __tablename__ = "vehicles"

    license_plate = Column(String, primary_key=True, index=True, nullable=False)
    brand = Column(String, nullable=False)
    model = Column(String, nullable=False)
    year = Column(Integer, nullable=False)
    color = Column(String, nullable=False)
    engine = Column(String, nullable=False)
    trim = Column(String, nullable=False)

    # Relation bidirectionnelle many-to-many vers le modèle User via la
    # table d'association ``user_favorites``. L'attribut ``back_populates``
    # assure la synchronisation automatique avec ``User.favorites``.
    favorited_by = relationship("User", secondary=user_favorites, back_populates="favorites"
)

def create_tables():
    """
    Créer l'ensemble des tables définies dans les modèles ORM.

    Exécute ``Base.metadata.create_all`` pour générer les tables SQL
    correspondant à tous les modèles ORM héritant de ``Base`` (``User``,
    ``Prediction``). Les tables existantes ne sont pas recréées ni
    modifiées (comportement ``CREATE TABLE IF NOT EXISTS``).

    Cette fonction est typiquement appelée au démarrage de l'application
    ou dans un script d'initialisation de la base de données.
    """
    Base.metadata.create_all(bind=engine)
    print("✅ Tables créées avec succès")


def get_db():
    """
    Fournir une session SQLAlchemy avec fermeture automatique.

    Générateur de dépendance FastAPI créant une session SQLAlchemy à
    partir de la fabrique ``SessionLocal``. La session est automatiquement
    fermée en fin de requête grâce au bloc ``try/finally``, garantissant
    la libération des connexions au pool même en cas d'erreur.

    Ce générateur est injecté via ``Depends(get_db)`` dans les endpoints
    et les fonctions de dépendance nécessitant un accès à la base de
    données.

    Yields:
        Session: Session SQLAlchemy active, prête à être utilisée pour
            des opérations de lecture et d'écriture sur la base de données.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()