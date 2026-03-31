package com.example.snaptaplaque.models.api.predictions;

import java.util.List;

/**
 * Modèle de données représentant la réponse de l'API pour l'historique des prédictions.
 * <p>Cette classe agit comme une enveloppe (wrapper) autour d'une liste d'objets
 * {@link HistoryResult}. Elle permet de récupérer l'ensemble des analyses de plaques
 * effectuées par l'utilisateur dans le passé, facilitant ainsi l'affichage d'une
 * liste chronologique dans l'application.</p>
 */
public class HistoryResponse {

    /** Liste contenant les résultats individuels de l'historique des scans. */
    private List<HistoryResult> history;

    /**
     * Constructeur pour initialiser la réponse de l'historique.
     *
     * @param history La liste d'objets {@link HistoryResult} récupérée depuis le serveur.
     */
    public HistoryResponse(List<HistoryResult> history) { this.history = history; }

    /**
     * Récupère la liste complète des entrées de l'historique.
     *
     * @return Une {@link List} contenant les résultats des prédictions passées.
     */
    public List<HistoryResult> getHistory() { return history; }
}