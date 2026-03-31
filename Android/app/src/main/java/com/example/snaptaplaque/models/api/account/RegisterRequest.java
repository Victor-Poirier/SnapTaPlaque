package com.example.snaptaplaque.models.api.account;

import com.google.gson.annotations.SerializedName;

/**
 * Modèle de données représentant une requête de création de compte (Inscription).
 * <p>Cette classe encapsule l'ensemble des informations requises par l'API pour enregistrer
 * un nouvel utilisateur. Elle inclut les identifiants de connexion, les informations
 * personnelles ainsi que les drapeaux de permissions et de conformité légale.</p>
 */
public class RegisterRequest {

    /**
     * Adresse e-mail de l'utilisateur servant à la communication et à la récupération de compte.
     */
    @SerializedName("email")
    private String email;

    /**
     * Identifiant unique choisi par l'utilisateur pour se connecter à l'application.
     */
    @SerializedName("username")
    private String username;

    /**
     * Mot de passe sécurisé choisi par l'utilisateur pour protéger son accès.
     */
    @SerializedName("password")
    private String password;

    /**
     * Identité complète de l'utilisateur (généralement Prénom et Nom).
     */
    @SerializedName("full_name")
    private String full_name;

    /**
     * Détermine si le compte créé doit disposer des privilèges d'administration.
     */
    @SerializedName("is_admin")
    private boolean is_admin;

    /**
     * État du consentement aux politiques de protection des données (RGPD).
     */
    @SerializedName("gdpr_consent")
    private boolean gdpr_consent;

    /**
     * Constructeur complet pour initialiser une demande d'inscription.
     *
     * @param email        L'adresse e-mail de l'utilisateur.
     * @param username     Le nom d'utilisateur unique.
     * @param password     Le mot de passe de l'utilisateur.
     * @param full_name    Le nom complet de l'utilisateur.
     * @param is_admin     {@code true} pour un compte administrateur, {@code false} sinon.
     * @param gdpr_consent {@code true} si l'utilisateur accepte la politique de confidentialité.
     */
    public RegisterRequest(String email,
                           String username,
                           String password,
                           String full_name,
                           boolean is_admin,
                           boolean gdpr_consent) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.full_name = full_name;
        this.is_admin = is_admin;
        this.gdpr_consent = gdpr_consent;
    }

    /**
     * Récupère l'adresse e-mail fournie dans la requête.
     *
     * @return L'adresse e-mail sous forme de {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Récupère le nom d'utilisateur choisi.
     *
     * @return Le nom d'utilisateur sous forme de {@link String}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Récupère le mot de passe défini pour l'inscription.
     *
     * @return Le mot de passe sous forme de {@link String}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Récupère le nom complet de l'utilisateur.
     *
     * @return Le nom complet sous forme de {@link String}.
     */
    public String getFull_name() {
        return full_name;
    }

    /**
     * Vérifie si le futur utilisateur demande des droits d'administration.
     *
     * @return {@code true} si l'utilisateur est admin, {@code false} sinon.
     */
    public boolean is_admin() {
        return is_admin;
    }

    /**
     * Vérifie si le consentement RGPD a été accordé.
     *
     * @return {@code true} si le consentement est validé, {@code false} sinon.
     */
    public boolean is_gdpr_consent() {
        return gdpr_consent;
    }
}