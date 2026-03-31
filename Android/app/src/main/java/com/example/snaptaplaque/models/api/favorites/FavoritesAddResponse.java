package com.example.snaptaplaque.models.api.favorites;

/**
 * Modèle de données représentant la réponse du serveur après l'ajout d'un véhicule aux favoris.
 * <p>Cette classe est utilisée pour réceptionner le message de confirmation
 * (ou d'erreur) renvoyé par l'API une fois que la demande d'enregistrement
 * d'une plaque d'immatriculation a été traitée.</p>
 */
public class FavoritesAddResponse {

    /** Message de statut renvoyé par l'API (ex: "Véhicule ajouté aux favoris"). */
    private String message;

    /**
     * Constructeur pour initialiser la réponse d'ajout aux favoris.
     *
     * @param message Le message descriptif du résultat de l'opération fourni par l'API.
     */
    public FavoritesAddResponse(String message) { this.message = message; }

    /**
     * Récupère le message de confirmation ou de retour du serveur.
     *
     * @return Le message de réponse sous forme de {@link String}.
     */
    public String getMessage() {
        return message;
    }
}
