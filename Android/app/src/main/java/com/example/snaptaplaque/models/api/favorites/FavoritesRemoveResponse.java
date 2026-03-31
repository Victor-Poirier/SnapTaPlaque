package com.example.snaptaplaque.models.api.favorites;

/**
 * Modèle de données représentant la réponse du serveur après la suppression d'un favori.
 * <p>Cette classe encapsule le message de confirmation renvoyé par l'API une fois
 * que l'association entre l'utilisateur et le véhicule (identifié par sa plaque)
 * a été révoquée dans la base de données.</p>
 */
public class FavoritesRemoveResponse {

    /** Message de confirmation ou d'erreur renvoyé par l'API (ex: "Favori supprimé avec succès"). */
    private String message;

    /**
     * Constructeur pour initialiser la réponse de suppression des favoris.
     *
     * @param message Le message descriptif du résultat de l'opération fourni par l'API.
     */
    public FavoritesRemoveResponse(String message) { this.message = message; }

    /**
     * Récupère le message de retour du serveur.
     *
     * @return Le message de réponse sous forme de {@link String}.
     */
    public String getMessage() {
        return message;
    }
}
