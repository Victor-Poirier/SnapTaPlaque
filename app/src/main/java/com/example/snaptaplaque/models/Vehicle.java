package com.example.snaptaplaque.models;

public class Vehicle {
    private String immatriculation;
    private String details;
    private boolean isFavorite;

    public Vehicle(String Limmatriculation, String LeDetails, boolean LeisFavorite) {
        this.immatriculation = Limmatriculation;
        this.details = LeDetails;
        this.isFavorite = LeisFavorite;

    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public String getDetails() {
        return details;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}
