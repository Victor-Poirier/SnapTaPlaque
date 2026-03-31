package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant la réponse du serveur après la suppression de la photo de profil.
 * <p>Cette classe permet de récupérer le message de confirmation envoyé par l'API
 * une fois que l'image de profil a été retirée avec succès du stockage serveur.</p>
 */
public class DeleteProfilePictureResponse {
    /** Message de confirmation ou d'erreur renvoyé par l'API. */
    private String message;

    /**
     * Constructeur pour initialiser la réponse de suppression de la photo.
     *
     * @param message Le message descriptif du résultat de l'opération fourni par l'API.
     */
    public DeleteProfilePictureResponse(String message) {
        this.message = message;
    }

    /**
     * Récupère le message de statut du serveur.
     *
     * @return Le message de réponse sous forme de {@link String}.
     */
    public String getMessage() {
        return message;
    }
}
