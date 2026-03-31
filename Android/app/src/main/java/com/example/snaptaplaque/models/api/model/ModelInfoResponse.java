package com.example.snaptaplaque.models.api.model;

import android.content.Context;

import com.example.snaptaplaque.R;

/**
 * Modèle de données représentant l'état et la configuration du modèle d'intelligence artificielle.
 * <p>Cette classe permet de vérifier si le moteur d'inférence est opérationnel,
 * de connaître l'architecture utilisée (YOLOv8, YOLOv12, etc.) et de décrire
 * le pipeline de traitement des images de plaques.</p>
 */
public class ModelInfoResponse {

    /** Indique si le modèle est chargé en mémoire et prêt à l'emploi. */
    private boolean loaded;

    /** Le type ou l'architecture du modèle de détection (ex: "YOLOv12"). */
    private String model_type;

    /** Description du pipeline de traitement (ex: "Détection + OCR Latin"). */
    private String pipeline;

    /**
     * Constructeur complet pour initialiser les informations du modèle.
     *
     * @param loaded     État de chargement du modèle.
     * @param pipeline   Description de la chaîne de traitement.
     * @param model_type Type d'architecture du modèle.
     */
    public ModelInfoResponse(boolean loaded, String pipeline, String model_type) {
        this.loaded = loaded;
        this.pipeline = pipeline;
        this.model_type = model_type;
    }

    /**
     * Génère une chaîne de caractères formatée pour l'affichage dans l'interface utilisateur.
     * * <p>Cette méthode utilise les ressources de chaînes (strings.xml) pour construire
     * un résumé lisible de la configuration technique du modèle.</p>
     *
     * @param context Le contexte Android pour accéder aux ressources de chaînes de caractères.
     * @return Une {@link String} multiligne contenant les détails du modèle.
     */
    public String createString(Context context){
        return  context.getString(R.string.model_type) + model_type + "\n" +
                context.getString(R.string.pipeline)   +  pipeline  + "\n" +
                context.getString(R.string.is_loaded)  + loaded + "\n" ;
    }

    /**
     * Vérifie si le modèle est chargé.
     *
     * @return {@code true} si le modèle est prêt, {@code false} sinon.
     */
    public boolean isLoaded() { return loaded; }

    /**
     * Récupère la description du pipeline.
     *
     * @return Le nom du pipeline sous forme de {@link String}.
     */
    public String getPipeline() { return pipeline; }

    /**
     * Récupère le type de modèle utilisé.
     *
     * @return Le type de modèle sous forme de {@link String}.
     */
    public String getModel_type() {
        return model_type;
    }
}
