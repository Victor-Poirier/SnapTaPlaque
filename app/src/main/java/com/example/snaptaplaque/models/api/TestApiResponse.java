package com.example.snaptaplaque.models.api;

/**
 * Schéma de retour de l'API de test de santé du backend.
 * Ce modèle correspond à la structure JSON suivante :
    {
        "status": "healthy",
        "version": "1.0"
        }
 */
public class TestApiResponse {

    private String status;

    private String version;

    public String getStatus() {
        return status;
    }


    public String getVersion() {
        return version;
    }
}
