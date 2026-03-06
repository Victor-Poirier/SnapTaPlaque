package com.example.snaptaplaque.models.api;

/**
 * Représente la réponse d'une requête d'inscription d'utilisateur.
 * Actuellement, cette classe est vide, mais elle peut être étendue à l'avenir pour inclure des informations supplémentaires
 * telles que le token d'authentification, les détails de l'utilisateur nouvellement créé, etc.
 */
public class RegisterResponse {

    /**
     * Message de confirmation ou d'erreur retourné par le serveur après une tentative d'inscription.
     */
    private String message;

    /**
     * Code de statut HTTP retourné par le serveur (ex: 200 pour succès, 400 pour erreur de validation, etc.).
     */
    private int statusCode;

    public RegisterResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * Récupère le code de statut HTTP de la réponse.
     * @return le code de statut HTTP
     */
    public int getStatusCode() {
        return statusCode;
    }


    /**
     * Récupère le message de la réponse.
     * @return le message de confirmation ou d'erreur
     */
    public String getMessage() {
        return message;
    }

}
