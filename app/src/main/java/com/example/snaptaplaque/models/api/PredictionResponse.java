package com.example.snaptaplaque.models.api;



import java.util.List;

/**
 * Représente la réponse de l'API de prédiction, contenant une liste de résultats de détection.
 */
public class PredictionResponse {

    /**
     * Liste des résultats de détection retournés par l'API.
     */
    private List<DetectionResult> result;

    public List<DetectionResult> getResults() {
        return result;
    }
}
