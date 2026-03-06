package com.example.snaptaplaque.models.api;

/**
 * Classe représentant la réponse de l'API lors de la connexion d'un utilisateur.
 * Contient le token d'accès nécessaire pour les requêtes authentifiées.
 */
public class LoginResponse {

    /**
     * Le token d'accès fourni par l'API après une connexion réussie.
     */
    private String access_token;

    /**
     * Récupère le token d'accès.
     *
     * @return le token d'accès
     */
    public String getAccessToken() {
        return access_token;
    }


}
