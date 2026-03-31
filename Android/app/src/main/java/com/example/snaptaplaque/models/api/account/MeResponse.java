package com.example.snaptaplaque.models.api.account;

/**
 * Modèle de données représentant les informations détaillées du profil de l'utilisateur connecté.
 * <p>Cette classe est généralement utilisée pour réceptionner les données de l'endpoint "/me".
 * Elle contient l'identité de l'utilisateur, son statut d'activité, ses privilèges d'administration
 * ainsi que les métadonnées de création de son compte.</p>
 */
public class MeResponse {
    /** Identifiant unique de l'utilisateur dans la base de données. */
    private Integer id;
    /** Adresse e-mail associée au compte utilisateur. */
    private String email;
    /** Nom d'utilisateur unique utilisé pour l'identification. */
    private String username;
    /** Nom complet (Prénom et Nom) de l'utilisateur. */
    private String full_name;
    /** Indique si le compte est actuellement actif et autorisé à se connecter. */
    private Boolean is_active;
    /** Indique si l'utilisateur possède les droits d'administration sur la plateforme. */
    private Boolean is_admin;
    /** Date et heure de création du compte (généralement au format ISO 8601). */
    private String created_at;

    /**
     * Constructeur complet pour initialiser les informations de profil.
     *
     * @param id         L'identifiant unique.
     * @param email      L'adresse e-mail.
     * @param username   Le nom d'utilisateur.
     * @param full_name  Le nom complet.
     * @param is_active  Le statut d'activité du compte.
     * @param is_admin   Le statut administrateur.
     * @param created_at La date de création du compte.
     */
    public MeResponse(Integer id, String email, String username, String full_name, Boolean is_active, Boolean is_admin, String created_at) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.full_name = full_name;
        this.is_active = is_active;
        this.is_admin = is_admin;
        this.created_at = created_at;
    }

    /**
     * Récupère l'identifiant unique de l'utilisateur.
     *
     * @return L'ID sous forme d'{@link Integer}.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Récupère l'adresse e-mail de l'utilisateur.
     *
     * @return L'adresse e-mail sous forme de {@link String}.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Récupère le nom d'utilisateur.
     *
     * @return Le pseudo sous forme de {@link String}.
     */
    public String getUsername() {
        return username;
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
     * Vérifie si le compte est actif.
     *
     * @return {@code true} si le compte est actif, {@code false} sinon.
     */
    public Boolean getIs_active() {
        return is_active;
    }

    /**
     * Vérifie si l'utilisateur possède les droits d'administration.
     *
     * @return {@code true} si l'utilisateur est admin, {@code false} sinon.
     */
    public Boolean getIs_admin() {
        return is_admin;
    }

    /**
     * Récupère la date de création du compte.
     *
     * @return La date de création sous forme de {@link String}.
     */
    public String getCreated_at() {
        return created_at;
    }
}
