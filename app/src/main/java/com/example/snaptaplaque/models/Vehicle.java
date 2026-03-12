package com.example.snaptaplaque.models;

/**
 * Représente un véhicule identifié par son immatriculation.
 * <p>
 * Cette classe encapsule les informations relatives à un véhicule,
 * notamment son numéro d'immatriculation, ses détails descriptifs
 * et son statut de favori.
 * </p>
 *
 */
public class Vehicle {

    /**
     * Le numéro d'immatriculation du véhicule.
     */
    private String immatriculation;

    /**
     * La marque de la voiture
     */
    private String brand;

    /**
     * Le modèle de la voiture
     */
    private String model;

    /**
     * Les informations complémentaires de la voiture
     */
    private String info;

    /**
     * L'énergie de la voiture
     */
    private String energy;

    /**
     * Indique si le véhicule est marqué comme favori par l'utilisateur.
     */
    private boolean isFavorite;

    /**
     * Construit une instance de l'objet {@link Vehicle} avec les informations fournies.
     * @param immatriculation L'immatriculation du véhicule
     * @param brand La marque du véhicule
     * @param model Le modèle du véhicule
     * @param info Les informations complémentaires du véhicule
     * @param energy L'énergie de la voiture
     * @param isFavorite Indique si la voiture est dans les favoris
     */
    public Vehicle(String immatriculation, String brand, String model, String info, String energy, boolean isFavorite) {
        this.immatriculation = immatriculation;
        this.brand = brand;
        this.model = model;
        this.info = info;
        this.energy = energy;
        this.isFavorite = isFavorite;
    }

    /**
     * Retourne le numéro d'immatriculation du véhicule.
     *
     * @return le numéro d'immatriculation sous forme de {@code String}
     */
    public String getImmatriculation() {
        return immatriculation;
    }

    /**
     * Retourne la marque du véhicule.
     * @return la marque du véhicule sous forme de {@code String}
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Retourne le modèle du véhicule.
     * @return le modèle du véhicule sous forme de {@code String}
     */
    public String getModel() {
        return model;
    }


    /**
     * Retourne les informations complémentaires du véhicule.
     * @return les informations complémentaires du véhicule sous forme de {@code String}
     */
    public String getInfo() {
        return info;
    }

    /**
     * Retourne l'énergie du véhicule.
     * @return l'énergie du véhicule sous forme de {@code String}
     */
    public String getEnergy() {
        return energy;
    }

    /**
     * Indique si le véhicule est marqué comme favori.
     *
     * @return {@code true} si le véhicule est un favori, {@code false} sinon
     */
    public boolean isFavorite() {
        return isFavorite;
    }

    /**
     * Met à jour l'état favori du véhicule
     *
     * @param favorite Le nouvel état favori à attribuer au véhicule ({@code true} pour favori, {@code false} sinon)
     */
    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }
}
