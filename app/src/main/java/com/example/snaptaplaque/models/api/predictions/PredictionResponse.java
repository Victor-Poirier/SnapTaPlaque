package com.example.snaptaplaque.models.api.predictions;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Représente la réponse de l'API de prédiction, contenant une liste de résultats de détection.
 */
public class PredictionResponse {

    /**
     * Liste des résultats de détection retournés par l'API.
     */
    @SerializedName("results")
    private List<PredictionDetectionResult> result;

    public PredictionResponse(List<PredictionDetectionResult> result) {
        this.result = result;
    }

    public List<PredictionDetectionResult> getResults() {
        return result;
    }
}
