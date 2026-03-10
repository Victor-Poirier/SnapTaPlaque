package com.example.snaptaplaque.models.api.account;

/**
 * Représente une requête d'inscription d'utilisateur.
 */
public class RegisterRequest {

    /**
     * Adresse email de l'utilisateur.
     */
    private String email;

    /**
     * Nom d'utilisateur choisi par l'utilisateur.
     */
    private String username;

    /**
     * Mot de passe choisi par l'utilisateur.
     */
    private String password;

    /**
     * Nom complet de l'utilisateur (prénom + nom).
     */
    private String fullName;

    /**
     * Indique si l'utilisateur est un administrateur (true) ou un utilisateur standard (false).
     */
    private Boolean isAdmin;

    /**
     * Indique si l'utilisateur à donner son consentement au RGPD (true) ou non (false).
     */
    private Boolean consent_rgpd;

    /**
     * Constructeur de la classe RegisterRequest.
     *
     * @param username Nom d'utilisateur choisi par l'utilisateur.
     * @param email    Adresse email de l'utilisateur.
     * @param password Mot de passe choisi par l'utilisateur.
     * @param nom      Nom complet de l'utilisateur (prénom + nom).
     * @param admin    Indique si l'utilisateur est un administrateur (true) ou un utilisateur standard (false).
     */
    public RegisterRequest(String username,
                            String email,
                            String password,
                            String nom,
                            Boolean admin,
                            Boolean consent) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.fullName = nom;
        this.isAdmin = false; // Par défaut, les utilisateurs ne sont pas des administrateurs
        this.consent_rgpd = consent;
    }


}
