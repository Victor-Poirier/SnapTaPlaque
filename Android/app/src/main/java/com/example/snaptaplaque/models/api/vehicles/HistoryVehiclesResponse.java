package com.example.snaptaplaque.models.api.vehicles;

import java.util.List;

/**
 * Modèle de données représentant la réponse de l'API pour l'historique détaillé des véhicules.
 * <p>Cette classe sert d'enveloppe (wrapper) pour une collection d'objets {@link InfoResponse}.
 * Elle permet à l'utilisateur de retrouver l'intégralité des fiches techniques des véhicules
 * qu'il a scannés ou enregistrés, facilitant ainsi la gestion d'un garage virtuel ou d'un
 * journal de bord complet.</p>
 */
public class HistoryVehiclesResponse {
    /** Liste des objets {@link InfoResponse} représentant les fiches techniques des véhicules. */
    private List<InfoResponse> history;

    /**
     * Constructeur pour initialiser la réponse avec une liste d'objets {@link InfoResponse}.
     *
     * @param history Une liste d'objets {@link InfoResponse} représentant les fiches techniques des véhicules.
     */
    public HistoryVehiclesResponse(List<InfoResponse> history) {
        this.history = history;
    }

    /**
     * Récupère la liste des objets {@link InfoResponse} représentant les fiches techniques des véhicules.
     *
     * @return Une {@link List} d'objets {@link InfoResponse}.
     */
    public List<InfoResponse> getHistory() {
        return history;
    }
}
