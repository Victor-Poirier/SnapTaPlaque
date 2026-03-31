package com.example.snaptaplaque.models.api.account;

import retrofit2.http.FormUrlEncoded;

/**
 * Modèle de données représentant une requête d'authentification (Login).
 * <p>Cette classe est utilisée pour envoyer les identifiants de l'utilisateur
 * au serveur lors de la phase de connexion. Elle structure les données afin
 * qu'elles soient traitées par l'API pour vérifier l'identité de l'utilisateur.</p>
 */
public class LoginRequest {
    /** Le nom d'utilisateur ou l'adresse e-mail servant d'identifiant. */
    private String username;
    /** Le mot de passe associé au compte utilisateur. */
    private String password;

    /**
     * Constructeur pour créer une requête de connexion complète.
     *
     * @param username L'identifiant (nom d'utilisateur ou e-mail) de l'utilisateur.
     * @param password Le mot de passe en clair (sera sécurisé lors du transport HTTPS).
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Récupère le nom d'utilisateur fourni pour la connexion.
     *
     * @return Le nom d'utilisateur sous forme de {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Récupère le mot de passe fourni pour la connexion.
     *
     * @return Le mot de passe sous forme de {@link String}.
     */
    public String getPassword() {
        return password;
    }
}
