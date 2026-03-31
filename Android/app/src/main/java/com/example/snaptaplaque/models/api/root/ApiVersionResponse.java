package com.example.snaptaplaque.models.api.root;

import android.content.Context;

import com.example.snaptaplaque.R;

/**
 * Modèle de données représentant la réponse de l'API concernant les informations de versionnage.
 * <p>Cette classe est utilisée pour récupérer l'état des versions disponibles sur le serveur
 * ainsi que l'identifiant de la version la plus récente. Elle joue un rôle crucial dans
 * la gestion du cycle de vie de l'application et la détection d'éventuelles mises à jour requises.</p>
 */
public class ApiVersionResponse {

    /** Objet contenant le détail des différentes versions supportées par l'API. */
    private ApiVersionResult versions;

    /** Chaîne de caractères indiquant la version la plus récente de l'API (ex: "v1.2.0"). */
    private String latest;

    /**
     * Constructeur complet pour initialiser la réponse de versionnage.
     *
     * @param versions L'objet détaillant les versions disponibles.
     * @param latest   Le tag de la version la plus récente.
     */
    public ApiVersionResponse(ApiVersionResult versions, String latest) {
        this.versions = versions;
        this.latest = latest;
    }

    /**
     * Récupère l'identifiant de la dernière version disponible.
     *
     * @return La version la plus récente sous forme de {@link String}.
     */
    public String getLatest() { return latest; }

    /**
     * Récupère l'objet contenant le détail des versions.
     *
     * @return Un objet {@link ApiVersionResult}.
     */
    public ApiVersionResult getVersions() { return versions; }

    /**
     * Génère une chaîne de caractères formatée pour l'affichage des versions dans l'interface.
     * * <p>Cette méthode concatène les détails de l'objet {@code versions} avec le libellé
     * de la version la plus récente récupéré depuis les ressources du projet.</p>
     *
     * @param context Le contexte Android pour l'accès aux ressources de chaînes de caractères.
     * @return Une {@link String} formatée pour l'utilisateur.
     */
    public String createString(Context context){
        return  getVersions().createString(context) + "\n" +
                context.getString(R.string.api_version_response_latest) + getLatest();
    }
}
