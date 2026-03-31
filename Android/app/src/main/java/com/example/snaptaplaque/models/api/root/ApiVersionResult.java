package com.example.snaptaplaque.models.api.root;

import android.content.Context;

import com.example.snaptaplaque.R;

/**
 * Modèle de données détaillant les caractéristiques d'une version spécifique de l'API.
 * <p>Cette classe fournit des informations techniques sur l'état opérationnel du serveur,
 * le numéro de version précis et la configuration du pipeline de traitement d'images
 * actuellement actif.</p>
 */
public class ApiVersionResult {

    /** Le numéro de version sémantique (ex: "1.0.2"). */
    private String version;

    /** L'état actuel de l'API (ex: "stable", "maintenance", "deprecated"). */
    private String status;

    /** Le nom ou l'identifiant du pipeline de traitement utilisé par cette version */
    private String pipeline;

    /**
     * Constructeur complet pour initialiser les détails d'une version d'API.
     *
     * @param version  Le numéro de version.
     * @param pipeline Le pipeline de traitement associé.
     * @param status   Le statut opérationnel.
     */
    public ApiVersionResult(String version, String pipeline, String status) {
        this.version = version;
        this.pipeline = pipeline;
        this.status = status;
    }

    /**
     * Récupère le numéro de version.
     *
     * @return La version sous forme de {@link String}.
     */
    public String getVersion() { return version; }

    /**
     * Récupère le statut opérationnel de l'API.
     *
     * @return Le statut sous forme de {@link String}.
     */
    public String getStatus() { return status; }

    /**
     * Récupère l'identifiant du pipeline de traitement.
     *
     * @return Le pipeline sous forme de {@link String}.
     */
    public String getPipeline() { return pipeline; }

    /**
     * Génère une chaîne de caractères formatée et localisée pour l'affichage technique.
     * <p>Cette méthode extrait les libellés depuis les ressources Android (strings.xml)
     * pour présenter la version, le statut et le pipeline sur plusieurs lignes.</p>
     *
     * @param context Le contexte Android pour accéder aux ressources de chaînes.
     * @return Une {@link String} multiligne récapitulant les informations.
     */
    public String createString(Context context){
        return  context.getString(R.string.api_version_result_version) + getVersion() + "\n" +
                context.getString(R.string.api_version_result_statut) + getStatus() + "\n" +
                        context.getString(R.string.api_version_result_pipeline) + getPipeline() + "\n";
    }
}
