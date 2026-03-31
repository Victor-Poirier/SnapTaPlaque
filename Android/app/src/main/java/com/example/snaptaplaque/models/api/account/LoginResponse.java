package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant la réponse du serveur suite à une connexion réussie.
 * <p>Cette classe encapsule le jeton d'authentification (Access Token) renvoyé par
 * l'API. Ce jeton est indispensable pour accéder aux ressources protégées du serveur
 * et doit généralement être inclus dans l'en-tête "Authorization" des requêtes HTTP.</p>
 */
public class LoginResponse {

    /**
     * Le jeton d'accès (souvent au format JWT) généré par le serveur pour
     * identifier et autoriser l'utilisateur.
     */
    private String access_token;

    /**
     * Récupère le jeton d'accès fourni par l'API.
     *
     * @return Le jeton d'accès sous forme de {@link String}.
     */
    public String getAccessToken() {
        return access_token;
    }

    /**
     * Constructeur pour initialiser la réponse avec le jeton d'accès.
     *
     * @param access_token Le jeton d'authentification généré par le serveur.
     */
    public LoginResponse(String access_token) {
        this.access_token = access_token;
    }
}
