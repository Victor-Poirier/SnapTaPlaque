package com.example.snaptaplaque.models.api.predictions;

/**
 * Modèle de données représentant les statistiques globales des prédictions d'un utilisateur.
 * <p>Cette classe est utilisée pour récupérer des données agrégées, comme le volume
 * total d'analyses effectuées, permettant d'alimenter des tableaux de bord ou des
 * indicateurs de progression dans le profil utilisateur.</p>
 */
public class StatsResponse {

    /** Le nombre total de prédictions (scans de plaques) réalisées par l'utilisateur. */
    private Integer total_predictions;

    /**
     * Constructeur pour initialiser la réponse statistique.
     *
     * @param total_predictions Le cumul des prédictions enregistrées sur le compte.
     */
    public StatsResponse(Integer total_predictions) {
        this.total_predictions = total_predictions;
    }

    /**
     * Récupère le nombre total de prédictions effectuées.
     *
     * @return Le total sous forme d'{@link Integer}.
     */
    public Integer getTotal_predictions() { return total_predictions; }
}