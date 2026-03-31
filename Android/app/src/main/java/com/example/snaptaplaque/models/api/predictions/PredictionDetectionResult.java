package com.example.snaptaplaque.models.api.predictions;

import com.google.gson.annotations.SerializedName;

/**
 * Modèle de données représentant un résultat de détection individuel issu de l'analyse d'image.
 * <p>Cette classe contient les données brutes extraites par le moteur d'OCR (Reconnaissance
 * Optique de Caractères) : le texte identifié sur la plaque d'immatriculation et le
 * score de confiance statistique associé à cette lecture.</p>
 */
public class PredictionDetectionResult {

    /**
     * Le texte de la plaque d'immatriculation identifié par le modèle.
     * <p>Mappé depuis la clé JSON "plate_text".</p>
     */
    @SerializedName("plate_text")
    private String plaque_number;

    /**
     * Le score de confiance de la détection.
     * <p>Représente la probabilité (entre 0.0 et 1.0) que le texte extrait soit correct.</p>
     */
    @SerializedName("confidence")
    private double confidence;

    /**
     * Constructeur pour initialiser un résultat de détection.
     *
     * @param plaque_number Le texte de la plaque reconnu.
     * @param confidence    Le niveau de certitude de la détection.
     */
    public PredictionDetectionResult(String plaque_number, double confidence) {
        this.plaque_number = plaque_number;
        this.confidence = confidence;
    }

    /**
     * Récupère le numéro de plaque détecté.
     *
     * @return Le numéro de plaque sous forme de {@link String}.
     */
    public String getPlaque_number() { return plaque_number; }

    /**
     * Définit le numéro de plaque détecté.
     *
     * @param plaque_number Le nouveau numéro de plaque.
     */
    public void setPlaque_number(String plaque_number) { this.plaque_number = plaque_number; }

    /**
     * Récupère le score de confiance de la détection.
     *
     * @return Le score sous forme de {@code double}.
     */
    public double getConfidence() { return confidence; }

    /**
     * Définit le score de confiance de la détection.
     *
     * @param confidence Le nouveau score de confiance.
     */
    public void setConfidence(double confidence) { this.confidence = confidence; }
}