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
     * Les détails descriptifs du véhicule (marque, modèle, couleur, etc.).
     */
    private String details;

    /**
     * Indique si le véhicule est marqué comme favori par l'utilisateur.
     */
    private boolean isFavorite;

    /**
     * Construit une nouvelle instance de {@code Vehicle} avec les informations spécifiées.
     *
     * @param Limmatriculation le numéro d'immatriculation du véhicule (ex. : "AB-123-CD")
     * @param LeDetails        les détails descriptifs du véhicule
     * @param LeisFavorite     {@code true} si le véhicule est marqué comme favori,
     *                         {@code false} sinon
     */
    public Vehicle(String Limmatriculation, String LeDetails, boolean LeisFavorite) {
        this.immatriculation = Limmatriculation;
        this.details = LeDetails;
        this.isFavorite = LeisFavorite;
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
     * Retourne les détails descriptifs du véhicule.
     *
     * @return les détails du véhicule sous forme de {@code String}
     */
    public String getDetails() {
        return details;
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
