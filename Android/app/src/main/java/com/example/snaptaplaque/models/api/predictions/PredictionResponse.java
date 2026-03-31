package com.example.snaptaplaque.models.api.predictions;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Modèle de données représentant la réponse globale de l'API de prédiction.
 * <p>Cette classe encapsule les résultats fournis par le moteur d'analyse d'image.
 * Elle contient une liste de {@link PredictionDetectionResult}, permettant de gérer
 * les cas où plusieurs plaques ou plusieurs hypothèses de lecture sont identifiées
 * sur une seule capture.</p>
 */
public class PredictionResponse {

    /**
     * Liste des résultats de détection extraits de l'image.
     * <p>Mappé depuis la clé JSON "results". Chaque élément de la liste détaille
     * le texte lu et l'indice de confiance associé.</p>
     */
    @SerializedName("results")
    private List<PredictionDetectionResult> result;

    /**
     * Constructeur pour initialiser la réponse de prédiction.
     *
     * @param result La liste des résultats de détection renvoyée par le serveur.
     */
    public PredictionResponse(List<PredictionDetectionResult> result) { this.result = result; }

    /**
     * Récupère la liste des résultats de détection.
     *
     * @return Une {@link List} d'objets {@link PredictionDetectionResult}.
     */
    public List<PredictionDetectionResult> getResults() { return result; }
}