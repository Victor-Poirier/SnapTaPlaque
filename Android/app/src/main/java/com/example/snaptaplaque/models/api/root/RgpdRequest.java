package com.example.snaptaplaque.models.api.root;

/**
 * Modèle de données représentant une requête pour obtenir les informations relatives au RGPD.
 * <p>Cette classe permet d'envoyer au serveur une demande ciblée pour récupérer
 * les politiques de confidentialité ou les conditions d'utilisation, adaptées
 * à la langue locale de l'utilisateur.</p>
 */
public class RgpdRequest {

    /** Code de langue utilisé pour localiser le texte juridique (ex: "fr", "en"). */
    String language;

    /**
     * Constructeur pour initialiser une requête de récupération des textes RGPD.
     *
     * @param language Le code de la langue souhaitée pour les documents légaux.
     */
    public RgpdRequest(String language) { this.language = language; }

    /**
     * Récupère le code de langue défini dans la requête.
     *
     * @return La langue sous forme de {@link String}.
     */
    public String getLanguage() { return language; }
}
