package com.example.snaptaplaque.models.api.vehicles;

/**
 * Modèle de données représentant une requête de consultation des informations d'un véhicule.
 * <p>Cette classe est le point d'entrée de la recherche technique. Elle encapsule le numéro
 * d'immatriculation (license plate) pour permettre au backend d'interroger les bases de données
 * d'immatriculation et de renvoyer les caractéristiques du véhicule correspondant.</p>
 */
public class InfoRequest {
    /** Numéro d'immatriculation du véhicule. */
    private String license_plate;

    /**
     * Constructeur pour initialiser la demande avec un numéro d'immatriculation.
     *
     * @param lp Numéro d'immatriculation du véhicule.
     */
    public InfoRequest(String lp){ this.license_plate = lp; }

    /**
     * Récupère le numéro d'immatriculation du véhicule.
     *
     * @return Le numéro d'immatriculation sous forme de {@link String}.
     */
    public String getLicense_plate() {
        return license_plate;
    }
}
