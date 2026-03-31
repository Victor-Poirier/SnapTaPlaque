package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant la réponse du serveur suite à une demande de changement de photo de profil.
 * * <p>Cette classe est utilisée pour désérialiser le flux JSON renvoyé par l'API
 * après l'envoi d'une nouvelle image. Elle contient généralement un message de confirmation
 * ou une description de l'état de l'opération.</p>
 */
public class ChangeProfilePictureResponse {

    /** Message renvoyé par le serveur (ex: "Image mise à jour avec succès"). */
    private String message;

    /**
     * Constructeur complet pour initialiser la réponse.
     *
     * @param message Le message de statut ou de confirmation provenant de l'API.
     */
    public ChangeProfilePictureResponse(String message) {
        this.message = message;
    }

    /**
     * Récupère le message de réponse du serveur.
     *
     * @return Une {@link String} contenant le message descriptif du résultat de l'opération.
     */
    public String getMessage() {
        return message;
    }
}