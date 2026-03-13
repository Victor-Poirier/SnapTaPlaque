package com.example.snaptaplaque.models.api.account;

import com.google.gson.annotations.SerializedName;

/**
 * Représente une requête d'inscription d'utilisateur.
 */
public class RegisterRequest {

    /**
     * Adresse email de l'utilisateur.
     */
    @SerializedName("email")
    private String email;

    /**
     * Nom d'utilisateur choisi par l'utilisateur.
     */
    @SerializedName("username")
    private String username;

    /**
     * Mot de passe choisi par l'utilisateur.
     */
    @SerializedName("password")
    private String password;

    /**
     * Nom complet de l'utilisateur (prénom + nom).
     */
    @SerializedName("full_name")
    private String full_name;

    /**
     * Indique si l'utilisateur est un administrateur (true) ou un utilisateur standard (false).
     */
    @SerializedName("is_admin")
    private boolean is_admin;

    /**
     * Indique si l'utilisateur à donner son consentement au RGPD (true) ou non (false).
     */
    @SerializedName("gdpr_consent")
    private boolean gdpr_consent;

    /**
     * Constructeur de la classe RegisterRequest.
     *
     * @param email L'adresse email de l'utilisateur.
     * @param username Le nom d'utilisateur choisi par l'utilisateur.
     * @param password Le mot de passe choisi par l'utilisateur.
     * @param full_name Le nom complet de l'utilisateur (prénom + nom).
     * @param is_admin Indique si l'utilisateur est un administrateur (true) ou un utilisateur standard (false).
     * @param gdpr_consent Indique si l'utilisateur à donner son consentement au RGPD (true) ou non (false).
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

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFull_name() {
        return full_name;
    }

    public boolean is_admin() {
        return is_admin;
    }

    public boolean is_gdpr_consent() {
        return gdpr_consent;
    }

}
