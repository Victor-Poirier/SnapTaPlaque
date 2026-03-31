package com.example.snaptaplaque.models.api.favorites;

import com.google.gson.annotations.SerializedName;

/**
 * Modèle de données représentant une requête pour retirer un véhicule des favoris.
 * <p>Cette classe contient les informations nécessaires pour identifier de manière
 * unique le favori à supprimer du compte de l'utilisateur, en se basant sur son
 * numéro d'immatriculation.</p>
 */
public class FavoritesRemoveRequest {

    /**
     * Le numéro d'immatriculation du véhicule à retirer (ex: "AA-123-BB").
     */
    private String licensePlate;

    /**
     * Constructeur pour initialiser une requête de suppression des favoris.
     *
     * @param lp Le numéro de plaque d'immatriculation du véhicule à retirer.
     */
    public FavoritesRemoveRequest(String lp) { this.licensePlate = lp; }

    /**
     * Récupère le numéro d'immatriculation ciblé par la suppression.
     *
     * @return La plaque d'immatriculation sous forme de {@link String}.
     */
    public String getLicensePlate() {
        return licensePlate;
    }
}
