package com.example.snaptaplaque.models.api.root;

/**
 * Modèle de données détaillant les droits fondamentaux de l'utilisateur sur ses données personnelles.
 * <p>Cette classe est encapsulée dans {@link RgpdResponse} et fournit les descriptions textuelles
 * ou les procédures relatives aux droits d'accès, d'effacement et de rectification,
 * conformément aux exigences du RGPD.</p>
 */
public class RgpdResultUserRight {
    /** Description du droit d'accès (permettant à l'utilisateur d'obtenir une copie de ses données). */
    private String access;

    /** Description du droit à l'effacement (également appelé "droit à l'oubli"). */
    private String erasure;

    /** Description du droit de rectification (permettant de corriger des données inexactes). */
    private String rectification;

    /**
     * Constructeur pour initialiser le détail des droits utilisateurs.
     *
     * @param access        Information sur le droit d'accès.
     * @param erasure       Information sur le droit à l'effacement.
     * @param rectification Information sur le droit de rectification.
     */
    public RgpdResultUserRight(String access, String erasure, String rectification) {
        this.access = access;
        this.erasure = erasure;
        this.rectification = rectification;
    }

    /**
     * Récupère les modalités du droit d'accès.
     *
     * @return Une {@link String} décrivant comment accéder aux données.
     */
    public String getAccess() {
        return access;
    }

    /**
     * Récupère les modalités du droit à l'effacement.
     *
     * @return Une {@link String} décrivant comment demander la suppression des données.
     */
    public String getErasure() {
        return erasure;
    }

    /**
     * Récupère les modalités du droit de rectification.
     *
     * @return Une {@link String} décrivant comment modifier ses informations personnelles.
     */
    public String getRectification() {
        return rectification;
    }
}
