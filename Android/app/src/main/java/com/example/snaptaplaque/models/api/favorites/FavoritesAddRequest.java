package com.example.snaptaplaque.models.api.favorites;

import com.google.gson.annotations.SerializedName;

/**
 * Modèle de données représentant une requête pour ajouter un véhicule aux favoris.
 * <p>Cette classe encapsule le numéro d'immatriculation nécessaire au serveur pour
 * identifier le véhicule à lier au compte de l'utilisateur connecté.</p>
 */
public class FavoritesAddRequest {

    /**
     * Le numéro d'immatriculation du véhicule (ex: "AA-123-BB").
     * <p>Utilise l'annotation {@link SerializedName} pour correspondre à la clé
     * attendue par l'API JSON ("license_plate").</p>
     */
    @SerializedName("license_plate")
    private String licensePlate;

    /**
     * Constructeur pour initialiser une requête d'ajout aux favoris.
     *
     * @param lp Le numéro de plaque d'immatriculation à enregistrer.
     */
    public FavoritesAddRequest(String lp) { this.licensePlate = lp; }

    /**
     * Récupère le numéro d'immatriculation contenu dans la requête.
     *
     * @return La plaque d'immatriculation sous forme de {@link String}.
     */
    public String getLicensePlate() { return licensePlate; }
}