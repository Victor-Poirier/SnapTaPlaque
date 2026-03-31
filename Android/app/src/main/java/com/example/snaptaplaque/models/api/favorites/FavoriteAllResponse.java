package com.example.snaptaplaque.models.api.favorites;

import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import java.util.List;

/**
 * Modèle de données représentant la réponse de l'API lors de la récupération de tous les favoris.
 * * <p>Cette classe encapsule une liste d'objets {@link InfoResponse}, permettant de
 * centraliser les informations techniques de tous les véhicules que l'utilisateur
 * a choisi de sauvegarder dans sa bibliothèque personnelle.</p>
 */
public class FavoriteAllResponse {

    /** * Liste contenant l'ensemble des véhicules marqués comme favoris.
     */
    private List<InfoResponse> favorites;

    /**
     * Constructeur pour initialiser la réponse avec la liste des favoris.
     *
     * @param favorites La liste des objets {@link InfoResponse} récupérée depuis le serveur.
     */
    public FavoriteAllResponse(List<InfoResponse> favorites) { this.favorites = favorites; }

    /**
     * Récupère la liste complète des véhicules favoris.
     *
     * @return Une {@link List} d'objets {@link InfoResponse} représentant les favoris de l'utilisateur.
     */
    public List<InfoResponse> getFavorites() { return favorites; }
}
