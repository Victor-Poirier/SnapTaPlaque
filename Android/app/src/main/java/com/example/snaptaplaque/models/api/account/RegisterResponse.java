package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant la réponse du serveur suite à une tentative d'inscription.
 * <p>Cette classe encapsule le résultat de l'opération de création de compte. Elle permet
 * à l'application de savoir si l'inscription a été acceptée (via le code de statut)
 * et d'afficher un message d'information ou d'erreur pertinent à l'utilisateur.</p>
 */
public class RegisterResponse {

    /**
     * Message textuel retourné par l'API (ex: "Utilisateur créé avec succès" ou "Cet email est déjà utilisé").
     */
    private String message;

    /**
     * Code de statut HTTP de la réponse (ex: 201 pour une création réussie, 400 pour une erreur de syntaxe).
     */
    private int statusCode;

    /**
     * Constructeur complet pour initialiser la réponse d'inscription.
     *
     * @param message    Le message de confirmation ou le détail de l'erreur.
     * @param statusCode Le code de statut HTTP associé à la réponse.
     */
    public RegisterResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * Récupère le code de statut HTTP retourné par le serveur.
     *
     * @return Le code de statut sous forme d'{@code int}.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Récupère le message descriptif de la réponse.
     *
     * @return Le message de confirmation ou d'erreur sous forme de {@link String}.
     */
    public String getMessage() {
        return message;
    }
}