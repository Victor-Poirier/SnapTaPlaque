package com.example.snaptaplaque.models.api.vehicles;

import com.example.snaptaplaque.models.Vehicle;

/**
 * Classe de réponse représentant les informations détaillées d'un véhicule
 * retournées par l'API REST.
 *
 * <p>Cette classe encapsule les données reçues des endpoints API relatifs aux véhicules,
 * notamment :
 * <ul>
 *     <li><strong>Historique des véhicules :</strong> endpoint {@code /v1/vehicles/history}</li>
 *     <li><strong>Véhicules favoris :</strong> endpoint {@code /v1/favorites/all}</li>
 *     <li><strong>Recherche de véhicule :</strong> endpoint {@code /v1/vehicles/info}</li>
 * </ul>
 * </p>
 *
 * <p>La classe fournit une méthode utilitaire {@link #createVehicles(boolean)} pour
 * convertir automatiquement les données de l'API vers le modèle interne {@link Vehicle}
 * utilisé par l'interface utilisateur et le {@link com.example.snaptaplaque.viewmodels.SharedViewModel}.</p>
 *
 * <h3>Format JSON attendu :</h3>
 * <pre>{@code
 * {
 *   "license_plate": "AB-123-CD",
 *   "brand": "Renault",
 *   "model": "Clio",
 *   "info": "1.2 TCe 100",
 *   "energy": "Essence"
 * }
 * }</pre>
 *
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * // Conversion depuis une réponse API vers un objet Vehicle
 * InfoResponse response = apiResponse.getVehicleInfo();
 * Vehicle vehicle = response.createVehicles(false); // Pas favori par défaut
 *
 * // Ou avec statut favori depuis la liste des favoris
 * Vehicle favoriteVehicle = response.createVehicles(true);
 * }</pre>
 *
 * @see Vehicle
 * @see com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse
 * @see com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse
 */
public class InfoResponse {

    /**
     * Le numéro d'immatriculation du véhicule au format français.
     *
     * <p>Exemples de formats valides :
     * <ul>
     *     <li><strong>Ancien format :</strong> {@code "123 ABC 12"}</li>
     *     <li><strong>Nouveau format :</strong> {@code "AB-123-CD"}</li>
     * </ul>
     * Cette valeur sert de clé unique pour identifier un véhicule dans
     * l'application et synchroniser les données entre l'historique et les favoris.</p>
     */
    private String license_plate;

    /**
     * La marque du véhicule (constructeur automobile).
     *
     * <p>Exemples : {@code "Renault"}, {@code "Peugeot"}, {@code "BMW"}, etc.
     * Cette information est utilisée dans l'affichage des détails du véhicule
     * et dans les fonctionnalités de recherche/filtrage.</p>
     */
    private String brand;

    /**
     * Le modèle du véhicule (gamme/série du constructeur).
     *
     * <p>Exemples : {@code "Clio"}, {@code "208"}, {@code "X3"}, etc.
     * Cette information complète la marque pour identifier précisément
     * le type de véhicule.</p>
     */
    private String model;

    /**
     * Les informations techniques complémentaires du véhicule.
     *
     * <p>Cette chaîne peut contenir diverses données techniques telles que :
     * <ul>
     *     <li><strong>Motorisation :</strong> {@code "1.2 TCe 100"}</li>
     *     <li><strong>Puissance :</strong> {@code "150 ch"}</li>
     *     <li><strong>Finition :</strong> {@code "Intens"}</li>
     * </ul>
     * Ces informations enrichissent l'affichage des détails du véhicule.</p>
     */
    private String info;

    /**
     * Le type d'énergie/carburant utilisé par le véhicule.
     *
     * <p>Valeurs typiques :
     * <ul>
     *     <li><strong>Thermiques :</strong> {@code "Essence"}, {@code "Diesel"}</li>
     *     <li><strong>Électriques :</strong> {@code "Électrique"}</li>
     *     <li><strong>Hybrides :</strong> {@code "Hybride"}, {@code "Hybride rechargeable"}</li>
     *     <li><strong>Autres :</strong> {@code "GPL"}, {@code "GNV"}</li>
     * </ul>
     * Cette information est affichée dans les détails du véhicule et peut
     * être utilisée pour des fonctionnalités de filtrage par type d'énergie.</p>
     */
    private String energy;

    /**
     * Construit une nouvelle instance {@code InfoResponse} avec tous les détails du véhicule.
     *
     * <p>Ce constructeur est typiquement utilisé par les frameworks de sérialisation
     * JSON (comme Gson ou Jackson) lors de la désérialisation des réponses API.
     * Il peut également être utilisé pour créer des instances de test ou des
     * données factices.</p>
     *
     * @param license_plate le numéro d'immatriculation du véhicule ; ne doit pas être {@code null}
     * @param brand         la marque du véhicule ; ne doit pas être {@code null}
     * @param model         le modèle du véhicule ; ne doit pas être {@code null}
     * @param info          les informations techniques complémentaires ; peut être {@code null} ou vide
     * @param energy        le type d'énergie/carburant ; ne doit pas être {@code null}
     */
    public InfoResponse(String license_plate, String brand, String model, String info, String energy) {
        this.license_plate = license_plate;
        this.brand = brand;
        this.model = model;
        this.info = info;
        this.energy = energy;
    }

    /**
     * Crée et retourne une instance {@link Vehicle} basée sur les données de cette réponse API.
     *
     * <p>Cette méthode utilitaire convertit les données brutes de l'API vers le modèle
     * interne {@link Vehicle} utilisé par l'interface utilisateur. Elle permet de
     * spécifier directement le statut favori du véhicule créé, évitant ainsi des
     * manipulations ultérieures.</p>
     *
     * <p>La conversion effectue les mappages suivants :
     * <ul>
     *     <li>{@link #license_plate} → {@link Vehicle#getImmatriculation()}</li>
     *     <li>{@link #brand} → {@link Vehicle#getBrand()}</li>
     *     <li>{@link #model} → {@link Vehicle#getModel()}</li>
     *     <li>{@link #info} → {@link Vehicle#getInfo()}</li>
     *     <li>{@link #energy} → {@link Vehicle#getEnergy()}</li>
     *     <li>{@code isFavorite} → {@link Vehicle#isFavorite()}</li>
     * </ul>
     * </p>
     *
     * <p><strong>Cas d'usage typiques :</strong>
     * <ul>
     *     <li><strong>Chargement de l'historique :</strong> {@code createVehicles(false)}
     *         car les véhicules ne sont pas favoris par défaut</li>
     *     <li><strong>Chargement des favoris :</strong> {@code createVehicles(true)}
     *         car tous proviennent de la liste des favoris</li>
     * </ul>
     * </p>
     *
     * @param isFavorite {@code true} si le véhicule doit être marqué comme favori,
     *                   {@code false} sinon
     * @return une nouvelle instance de {@link Vehicle} configurée avec les données
     *         de cette réponse API et le statut favori spécifié
     */
    public Vehicle createVehicles(boolean isFavorite){
        return new Vehicle(license_plate, brand, model, info, energy, isFavorite);
    }

    /**
     * Retourne le numéro d'immatriculation du véhicule.
     *
     * <p>Cette méthode est utilisée par le {@link com.example.snaptaplaque.viewmodels.SharedViewModel}
     * pour identifier de manière unique les véhicules lors de la synchronisation
     * entre l'historique et les favoris.</p>
     *
     * @return le numéro d'immatriculation ; ne sera jamais {@code null}
     */
    public String getLicensePlate() {
        return license_plate;
    }

    /**
     * Retourne la marque du véhicule.
     *
     * @return la marque du constructeur automobile ; ne sera jamais {@code null}
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Retourne le modèle du véhicule.
     *
     * @return le modèle/série du véhicule ; ne sera jamais {@code null}
     */
    public String getModel() {
        return model;
    }

    /**
     * Retourne les informations techniques complémentaires du véhicule.
     *
     * @return les détails techniques (motorisation, puissance, etc.) ;
     *         peut être {@code null} ou une chaîne vide si aucune information
     *         complémentaire n'est disponible
     */
    public String getInfo() {
        return info;
    }

    /**
     * Retourne le type d'énergie/carburant du véhicule.
     *
     * @return le type d'énergie (essence, diesel, électrique, etc.) ;
     *         ne sera jamais {@code null}
     */
    public String getEnergy() {
        return energy;
    }
}
