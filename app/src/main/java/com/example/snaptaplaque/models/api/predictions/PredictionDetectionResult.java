package com.example.snaptaplaque.models.api.predictions;

/**
 * Représente un résultat de détection retourné par l'API de prédiction, contenant le numéro de plaque détecté et la confiance associée.
 */
public class PredictionDetectionResult {
    private String plaque_number;
    private double confidence;

    public String getPlaque_number() {
        return plaque_number;
    }

    public void setPlaque_number(String plaque_number) {
        this.plaque_number = plaque_number;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
