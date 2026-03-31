package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant la réponse du serveur à une demande de suppression de compte.
 * <p>Cette classe encapsule le message de retour envoyé par l'API après qu'un utilisateur
 * a initié la procédure de suppression définitive de ses données personnelles et de son accès.</p>
 */
public class DeleteAccountResponse {
    /** Message de statut renvoyé par le service (ex: "Compte supprimé avec succès"). */
    private String message;

    /**
     * Récupère le message de confirmation ou d'erreur du serveur.
     *
     * @return Le message de réponse sous forme de {@link String}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Constructeur pour initialiser la réponse de suppression.
     *
     * @param message Le message descriptif du résultat de l'opération fourni par l'API.
     */
    public DeleteAccountResponse(String message) {
        this.message = message;
    }
}
