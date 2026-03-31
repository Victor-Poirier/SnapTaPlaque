"""
schemas.py — Schémas Pydantic de validation et de sérialisation de l'API SnapTaPlaque.

Ce module définit l'ensemble des schémas Pydantic utilisés pour la
validation des données entrantes (requêtes), la sérialisation des
données sortantes (réponses) et le transport d'informations internes
(tokens JWT). Ces schémas constituent le contrat d'interface de l'API
REST et alimentent la documentation OpenAPI générée automatiquement.

Catégories de schémas :
    - **Authentification** — Schémas de gestion de la connexion et des
      tokens JWT (``LoginRequest``, ``Token``).
    - **Prédictions**      — Schémas de validation et de représentation
      des résultats de détection de plaques d'immatriculation
      (``DetectionResult``, ``PredictionResponse``, ``PlateStats``,
      ``PlateHistory``).
    - **Utilisateur**      — Schémas de création et de représentation
      des utilisateurs (``UserCreate``, ``UserResponse``).

Version : 1.0.0
"""
from pydantic import BaseModel, EmailStr, Field, field_validator
from typing import Optional, List
from datetime import datetime


# ---------- AUTH ----------

class LoginRequest(BaseModel):
    """
    Schéma de validation pour une requête de connexion.

    Valide les identifiants soumis par l'utilisateur lors de la
    tentative d'authentification. Le nom d'utilisateur et le mot de
    passe sont transmis en clair dans le corps de la requête et
    vérifiés côté serveur via comparaison avec le hash bcrypt stocké
    en base de données.

    :ivar username: Nom d'utilisateur unique identifiant le compte
        à authentifier.
    :vartype username: str
    :ivar password: Mot de passe en clair de l'utilisateur, qui
        sera comparé au hash bcrypt stocké en base de données.
    :vartype password: str
    """

    username: str
    password: str


class Token(BaseModel):
    """
    Schéma de réponse pour un token JWT d'accès.

    Représente le token d'accès retourné à l'utilisateur après une
    authentification réussie. Ce schéma suit la convention OAuth2
    avec les champs ``access_token`` et ``token_type``.

    :ivar access_token: Token JWT signé contenant les informations
        d'identification de l'utilisateur (claims), utilisé pour
        authentifier les requêtes ultérieures via le header
        ``Authorization: Bearer <token>``.
    :vartype access_token: str
    :ivar token_type: Type du token, toujours ``"bearer"``
        conformément à la spécification OAuth2.
    :vartype token_type: str
    """

    access_token: str
    token_type: str = "bearer"


class TokenWithRefresh(Token):
    """
    Schéma de réponse pour un token JWT avec refresh token.

    Étend le schéma ``Token`` en ajoutant un refresh token permettant
    à l'utilisateur de renouveler son token d'accès sans se
    ré-authentifier. Le refresh token possède une durée de validité
    plus longue que le token d'accès.

    :ivar access_token: Token JWT d'accès (hérité de ``Token``).
    :vartype access_token: str
    :ivar token_type: Type du token, ``"bearer"`` (hérité de
        ``Token``).
    :vartype token_type: str
    :ivar refresh_token: Token JWT de rafraîchissement, utilisé
        pour obtenir un nouveau token d'accès lorsque celui-ci
        expire.
    :vartype refresh_token: str

    Configuration:
        ``json_schema_extra`` — Exemple de réponse intégré à la
        documentation OpenAPI illustrant la structure complète du
        token avec refresh.
    """

    refresh_token: str

    class Config:
        """
        Configuration interne du schéma Pydantic.

        :ivar json_schema_extra: Exemple de réponse intégré à la
            documentation OpenAPI pour faciliter la compréhension
            du format de retour.
        :vartype json_schema_extra: dict
        """

        json_schema_extra = {
            "example": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "bearer",
                "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
        }


class TokenData(BaseModel):
    """
    Schéma de transport des données extraites d'un token JWT.

    Représente les informations (claims) décodées à partir d'un token
    JWT après vérification de sa signature et de sa validité. Ce schéma
    est utilisé en interne par les dépendances d'authentification pour
    transmettre l'identité de l'utilisateur aux endpoints protégés.

    :ivar username: Nom d'utilisateur extrait du claim
        ``sub`` (subject) du token JWT. ``None`` si le claim est
        absent ou invalide.
    :vartype username: str | None
    :ivar token_type: Type du token extrait des claims
        personnalisés, permettant de distinguer les tokens d'accès
        (``"access"``) des tokens de rafraîchissement
        (``"refresh"``). ``None`` si le type n'est pas spécifié.
    :vartype token_type: str | None
    """

    username: Optional[str] = None
    token_type: Optional[str] = None  # "access" ou "refresh"

# ================= PREDICTIONS =================

class DetectionResult(BaseModel):
    """
    Schéma de représentation d'un résultat unitaire de détection de plaque.

    Représente les informations extraites par le pipeline de
    reconnaissance de plaques d'immatriculation (détection YOLO + OCR)
    pour une plaque unique détectée dans une image soumise.

    :ivar plate_text: Texte de la plaque d'immatriculation
        reconnu par le moteur OCR. ``None`` si aucune plaque n'a
        été détectée ou si la lecture OCR a échoué.
    :vartype plate_text: str | None
    :ivar confidence: Score de confiance de la détection,
        compris entre 0.0 et 1.0. ``None`` si aucune plaque n'a
        été détectée.
    :vartype confidence: float | None
    :ivar bounding_box: Dictionnaire contenant les
        coordonnées de la boîte englobante de la plaque détectée
        dans l'image. ``None`` si les coordonnées ne sont pas
        disponibles.
    :vartype bounding_box: dict | None
    """

    plate_text: Optional[str] = None
    confidence: Optional[float] = None
    bounding_box: Optional[dict] = None


class PredictionResponse(BaseModel):
    """
    Schéma de sérialisation pour la représentation d'une prédiction.

    Utilisé pour les réponses API retournant les informations complètes
    d'une prédiction de reconnaissance de plaque, incluant les
    métadonnées (identifiant, utilisateur, fichier, date) et la liste
    des résultats de détection associés.

    :ivar id: Identifiant unique auto-incrémenté de la prédiction
        (clé primaire).
    :vartype id: int
    :ivar user_id: Identifiant de l'utilisateur ayant soumis la
        prédiction (clé étrangère vers ``users.id``).
    :vartype user_id: int
    :ivar filename: Nom du fichier image soumis pour la détection
        de plaque d'immatriculation.
    :vartype filename: str
    :ivar results: Liste des résultats de
        détection de plaques contenus dans l'image. Chaque élément
        représente une plaque détectée avec son texte, son score
        de confiance et ses coordonnées.
    :vartype results: list[DetectionResult]
    :ivar created_at: Date et heure de création de la
        prédiction. Définie automatiquement à l'instant de
        l'insertion en base de données.
    :vartype created_at: datetime

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``Prediction`` (anciennement ``orm_mode``).
    """

    id: int
    user_id: int
    filename: str
    results: List[DetectionResult]
    created_at: datetime

    class Config:
        from_attributes = True


class PlateStats(BaseModel):
    """
    Statistiques de prédiction d'un utilisateur.

    Schéma utilisé pour représenter les statistiques agrégées de
    détection de plaques d'un utilisateur, incluant le nombre total
    de prédictions effectuées.

    :ivar total_predictions: Nombre total de prédictions de
        reconnaissance de plaques effectuées par l'utilisateur.
    :vartype total_predictions: int
    """

    total_predictions: int


class PlateHistory(BaseModel):
    """
    Schéma de sérialisation pour l'historique simplifié des prédictions.

    Représente une entrée dans l'historique des prédictions d'un
    utilisateur, sous une forme allégée ne contenant que les
    informations essentielles (identifiant, texte de la plaque,
    confiance, date). Ce schéma est utilisé pour les endpoints de
    consultation d'historique paginé.

    :ivar id: Identifiant unique auto-incrémenté de la prédiction
        (clé primaire).
    :vartype id: int
    :ivar plate_text: Texte de la plaque d'immatriculation
        reconnu. ``None`` si aucune plaque n'a été détectée ou si
        la lecture OCR a échoué.
    :vartype plate_text: str | None
    :ivar confidence: Score de confiance de la détection,
        compris entre 0.0 et 1.0. ``None`` si aucune plaque n'a
        été détectée.
    :vartype confidence: float | None
    :ivar created_at: Date et heure de création de la
        prédiction.
    :vartype created_at: datetime

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``Prediction`` (anciennement ``orm_mode``).
    """

    id: int
    plate_text: Optional[str] = None
    confidence: Optional[float] = None
    created_at: datetime

    class Config:
        from_attributes = True


class PredictionHistory(BaseModel):
    history: List[PlateHistory] 

    class Config:
        from_attributes = True
    
# ---------- USER ----------

class UserCreate(BaseModel):
    """
    Schéma de validation pour la création d'un nouvel utilisateur.

    Valide les données soumises lors de l'inscription d'un utilisateur.
    L'adresse email est automatiquement validée par le type ``EmailStr``
    de Pydantic. Le mot de passe est transmis en clair et sera haché
    via bcrypt avant d'être stocké en base de données.

    :ivar email: Adresse email unique de l'utilisateur, validée
        automatiquement par le type ``EmailStr`` de Pydantic.
    :vartype email: EmailStr
    :ivar username: Nom d'utilisateur unique identifiant le compte
        sur la plateforme.
    :vartype username: str
    :ivar password: Mot de passe en clair de l'utilisateur. Sera
        haché via bcrypt avant d'être persisté en base de données.
    :vartype password: str
    :ivar full_name: Nom complet de l'utilisateur (prénom et nom).
    :vartype full_name: str
    :ivar is_admin: Indique si l'utilisateur doit être créé avec
        les privilèges d'administration. Par défaut ``False``.
    :vartype is_admin: bool
    :ivar gdpr_consent: Indique si l'utilisateur a explicitement
        accepté la politique de confidentialité (RGPD). Doit être
        ``True`` pour permettre la création du compte, conformément
        aux exigences légales.
    :vartype gdpr_consent: bool
    """

    email: EmailStr
    username: str = Field(..., min_length=3, max_length=50)
    password: str = Field(..., min_length=5, max_length=128)
    full_name: str
    is_admin: bool = False

    # RGPD : L'utilisateur doit explicitement accepter la politique de
    # confidentialité lors de l'inscription.
    gdpr_consent: bool

    @field_validator("username")
    @classmethod
    def validate_username(cls, v):
        """
        Valider le format du nom d'utilisateur.

        Vérifie que la valeur fournie ne contient que des caractères
        alphanumériques (lettres ASCII et chiffres). Les caractères
        spéciaux, espaces et signes de ponctuation sont interdits.

        :param v: Valeur du champ ``username`` à valider.
        :type v: str

        :return: Nom d'utilisateur validé, inchangé.
        :rtype: str

        :raises ValueError: Si le nom d'utilisateur contient des caractères
            non alphanumériques.
        """
        if not v.isalnum():
            raise ValueError("Username must be alphanumeric")
        return v

class UserResponse(BaseModel):
    """
    Schéma de sérialisation pour la représentation d'un utilisateur.

    Utilisé pour les réponses API retournant les informations d'un
    utilisateur. Ce schéma exclut volontairement le mot de passe haché
    afin de ne jamais exposer cette donnée sensible via l'API.

    :ivar id: Identifiant unique auto-incrémenté de l'utilisateur.
    :vartype id: int
    :ivar email: Adresse email de l'utilisateur.
    :vartype email: str
    :ivar username: Nom d'utilisateur unique.
    :vartype username: str
    :ivar full_name: Nom complet de l'utilisateur (prénom et
        nom). ``None`` si non renseigné.
    :vartype full_name: str | None
    :ivar is_active: Indique si le compte de l'utilisateur est
        actif. Un compte désactivé ne peut pas s'authentifier.
    :vartype is_active: bool
    :ivar is_admin: Indique si l'utilisateur possède les privilèges
        d'administration.
    :vartype is_admin: bool
    :ivar created_at: Date et heure de création du compte
        utilisateur.
    :vartype created_at: datetime

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``User`` (anciennement ``orm_mode``).
    """

    id: int
    email: str
    username: str
    full_name: Optional[str]
    is_active: bool
    is_admin: bool
    created_at: datetime

    class Config:
        from_attributes = True

# ================== Vehicles Info ==================
class VehicleInfoResponse(BaseModel):
    """
    Schéma de sérialisation pour les informations détaillées d'un véhicule.

    Utilisé pour les réponses API retournant les informations complètes
    d'un véhicule identifié par sa plaque d'immatriculation. Ce schéma
    est typiquement retourné par l'endpoint ``GET /vehicles/info`` après
    une recherche en base de données à partir de la plaque détectée par
    le pipeline de reconnaissance.

    :ivar license_plate: Plaque d'immatriculation du véhicule
        (identifiant unique en base de données).
    :vartype license_plate: str
    :ivar brand: Marque du véhicule (ex. : Renault, Peugeot, BMW).
    :vartype brand: str
    :ivar model: Modèle du véhicule (ex. : Clio, 308, Série 3).
    :vartype model: str
    :ivar info: Informations complémentaires sur le véhicule
    :vartype info: str
    :ivar energy: Type d'énergie du véhicule (ex. : Essence, Diesel,
        Électrique).
    :vartype energy: str

    Configuration:
        ``from_attributes`` — Active la compatibilité avec les instances
        ORM SQLAlchemy, permettant de construire le schéma directement
        à partir d'un objet ``Vehicle`` (anciennement ``orm_mode``).
    """

    license_plate: str
    brand: str
    model: str
    info: str
    energy: str

    class Config:
        from_attributes = True

class AllFavoritesResponse(BaseModel):
    """
    Schéma de sérialisation pour la liste complète des plaques favorites d'un utilisateur.

    Représente la réponse d'une requête retournant toutes les plaques
    favorites d'un utilisateur, incluant les informations détaillées de
    chaque plaque (texte, marque, modèle, énergie).

    :ivar favorites: Liste des plaques favorites de l'utilisateur, 
        avec leurs informations détaillées.
    :vartype favorites: list[VehicleInfoResponse]
    """
    favorites: List[VehicleInfoResponse]

    class Config:
        from_attributes = True

class VehicleInfoHistoryResponse(BaseModel):
    """
    Schéma de sérialisation pour l'historique des informations de véhicules consultés ou enregistrés par l'utilisateur.

    Représente la réponse d'une requête retournant l'historique des
    plaques d'immatriculation consultées ou enregistrées par un
    utilisateur, avec les détails de chaque véhicule.

    :ivar history: Liste des entrées d'historique, chacune contenant les détails 
        d'un véhicule consulté ou enregistré par l'utilisateur.
    :vartype history: list[VehicleInfoResponse]
    """
    history: List[VehicleInfoResponse]

    class Config:
        from_attributes = True

class RGPDRequest(BaseModel):
    """
    Schéma de validation pour une requête de consentement RGPD.
    Valide les données soumises par l'utilisateur pour donner son consentement explicite à la politique de confidentialité (RGPD) lors de l'inscription ou de la
    modification de son compte. L'utilisateur doit accepter la politique pour pouvoir créer ou maintenir son compte actif.
    
    :ivar language: Langue préférée de l'utilisateur pour la
        communication de la politique de confidentialité. Permet
        d'adapter le contenu de la politique en fonction de la
        langue choisie par l'utilisateur.
    :vartype language: str
    """
    language: str

    class Config:
        from_attributes = True