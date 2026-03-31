package com.example.snaptaplaque.models.api.root;

import com.example.snaptaplaque.R;

import java.util.List;

import android.content.Context;

/**
 * Modèle de données représentant la réponse complète des informations de conformité RGPD.
 * <p>Cette classe centralise l'ensemble des mentions légales obligatoires : responsable du traitement,
 * finalités, base légale, conservation et droits des utilisateurs. Elle est conçue pour
 * transformer les données structurées reçues de l'API en chaînes de caractères formatées
 * prêtes à être affichées dans une interface Android (TextView, Dialog, etc.).</p>
 */
public class RgpdResponse {

    /** Le responsable du traitement des données (entité ou entreprise). */
    private final String controller;
    /** Liste des moyens de contact (e-mails, adresses, etc.). */
    private final List<String> contact;
    /** La finalité du traitement (pourquoi les données sont collectées). */
    private final String purpose;
    /** La base juridique justifiant le traitement (ex: consentement, contrat). */
    private final String legal_basis;
    /** Liste des catégories de données personnelles collectées. */
    private final List<String> data_collected;
    /** La durée de conservation des données avant suppression ou anonymisation. */
    private final String retention_period;
    /** Détail des droits spécifiques de l'utilisateur (accès, rectification, effacement). */
    private final RgpdResultUserRight user_rights;
    /** Informations sur le partage des données avec des tiers ou des sous-traitants. */
    private final String data_sharing;
    /** Liste des mesures techniques et organisationnelles pour protéger les données. */
    private final List<String> security_measures;

    /**
     * Constructeur complet pour initialiser la réponse RGPD.
     *
     * @param controller        Responsable du traitement.
     * @param contact           Liste des contacts.
     * @param purpose           Finalité du traitement.
     * @param legal_basis       Base légale.
     * @param data_collected    Liste des données collectées.
     * @param retention_period  Durée de conservation.
     * @param user_rights       Objet détaillant les droits de l'utilisateur.
     * @param data_sharing      Détails du partage de données.
     * @param security_measures Liste des mesures de sécurité.
     */
    public RgpdResponse(String controller, List<String> contact, String purpose, String legal_basis, List<String> data_collected, String retention_period, RgpdResultUserRight user_rights, String data_sharing, List<String> security_measures) {
        this.controller = controller;
        this.contact = contact;
        this.purpose = purpose;
        this.legal_basis = legal_basis;
        this.data_collected = data_collected;
        this.retention_period = retention_period;
        this.user_rights = user_rights;
        this.data_sharing = data_sharing;
        this.security_measures = security_measures;
    }

    /**
     * Génère une présentation textuelle complète et hiérarchisée de la politique de confidentialité.
     * * <p>Cette méthode concatène toutes les sections de la réponse en utilisant les ressources
     * de chaînes de caractères (strings.xml) pour assurer une présentation localisée et lisible.</p>
     *
     * @param context Le contexte Android pour accéder aux ressources de traduction.
     * @return Une {@link String} formatée avec des retours à la ligne et des puces d'énumération.
     */
    public String createString(Context context) {
        return  context.getString(R.string.rgpd_controller) + controller + "\n\n" +
                context.getString(R.string.rgpd_contact) + "\n" + printContact() + "\n\n" +
                context.getString(R.string.rgpd_purpose) + purpose + "\n\n" +
                context.getString(R.string.rgpd_legal_basis) + legal_basis + "\n\n" +
                context.getString(R.string.rgpd_data_collected) + "\n" + printDataCollected() + "\n\n" +
                context.getString(R.string.rgpd_retention_period) + retention_period + "\n\n" +
                context.getString(R.string.rgpd_user_rights) + printUserRights(context) + "\n\n" +
                context.getString(R.string.rgpd_data_sharing) + data_sharing + "\n\n" +
                context.getString(R.string.rgpd_security_measures) + "\n" + printSecurityMeasures() + "\n";
    }

    /**
     * Formate la liste des données collectées sous forme de liste à puces.
     *
     * @return Une chaîne formatée.
     */
    public String printDataCollected() {
        StringBuilder sb = new StringBuilder();
        for (String data : data_collected) {
            sb.append("\t- ").append(data).append("\n");
        }
        return sb.toString();
    }

    /**
     * Formate la liste des mesures de sécurité sous forme de liste à puces.
     *
     * @return Une chaîne formatée.
     */
    public String printSecurityMeasures() {
        StringBuilder sb = new StringBuilder();
        for (String measure : security_measures) {
            sb.append("\t- ").append(measure).append("\n");
        }
        return sb.toString();
    }

    /**
     * Formate la liste des contacts sous forme de liste à puces.
     *
     * @return Une chaîne formatée.
     */
    public String printContact() {
        StringBuilder sb = new StringBuilder();
        for (String contact : contact) {
            sb.append("\t- ").append(contact).append("\n");
        }
        return sb.toString();
    }

    /**
     * Extrait et formate les droits spécifiques de l'utilisateur.
     *
     * @param context Le contexte Android pour accéder aux libellés des droits.
     * @return Une chaîne détaillant l'accès, l'effacement et la rectification.
     */
    public String printUserRights(Context context) {
        return "\n\t- " + context.getString(R.string.rgpd_user_rights_access) + ": " + user_rights.getAccess() + "\n" +
                "\t- " + context.getString(R.string.rgpd_user_rights_erasure) + ": " + user_rights.getErasure() + "\n" +
                "\t- " + context.getString(R.string.rgpd_user_rights_rectification) + ": " + user_rights.getRectification() + "\n";
    }

    /**
     * Récupère le nom du responsable du traitement.
     *
     * @return Le nom du responsable sous forme de {@link String}.
     */
    public String getController() {
        return controller;
    }

    /**
     * Récupère la liste des moyens de contact.
     *
     * @return Une {@link List} de {@link String}.
     */
    public List<String> getContact() {
        return contact;
    }

    /**
     * Récupère la finalité du traitement.
     *
     * @return La finalité sous forme de {@link String}.
     */
    public String getPurpose() {
        return purpose;
    }

    /**
     * Récupère la base juridique.
     *
     * @return La base juridique sous forme de {@link String}.
     */
    public String getLegal_basis() {
        return legal_basis;
    }

    /**
     * Récupère la liste des données collectées.
     *
     * @return Une {@link List} de {@link String}.
     */
    public List<String> getData_collected() {
        return data_collected;
    }

    /**
     * Récupère la durée de conservation des données.
     *
     * @return La durée sous forme de {@link String}.
     */
    public String getRetention_period() {
        return retention_period;
    }

    /**
     * Récupère les droits spécifiques de l'utilisateur.
     *
     * @return Un objet {@link RgpdResultUserRight}.
     */
    public RgpdResultUserRight getUser_rights() {
        return user_rights;
    }

    /**
     * Récupère les détails sur le partage des données.
     *
     * @return Les détails sous forme de {@link String}.
     */
    public String getData_sharing() {
        return data_sharing;
    }

    /**
     * Récupère la liste des mesures de sécurité.
     *
     * @return Une {@link List} de {@link String}.
     */
    public List<String> getSecurity_measures() {
        return security_measures;
    }
}
