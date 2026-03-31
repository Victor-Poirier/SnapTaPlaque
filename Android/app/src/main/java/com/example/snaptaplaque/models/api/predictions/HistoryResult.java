package com.example.snaptaplaque.models.api.predictions;

/**
 * Modèle de données représentant une entrée unique dans l'historique des scans de l'utilisateur.
 * <p>Cette classe contient les détails d'une prédiction passée, incluant le texte
 * de la plaque identifié, le score de confiance algorithmique et la date
 * à laquelle l'analyse a été effectuée.</p>
 */
public class HistoryResult {

    /** Identifiant unique de l'entrée d'historique en base de données. */
    private Integer id;

    /** Le texte de la plaque d'immatriculation reconnu par le modèle OCR. */
    private String plate_text;

    /** Le niveau de confiance de la prédiction (généralement compris entre 0.0 et 1.0).
     * Représente la certitude du modèle quant à l'exactitude de la détection.
     */
    private Float confidence;

    /** La date et l'heure de création du scan (format ISO 8601). */
    private String created_at;

    /**
     * Constructeur complet pour initialiser un résultat d'historique.
     *
     * @param id         L'identifiant unique du scan.
     * @param plate_text Le texte de la plaque reconnu.
     * @param confidence L'indice de fiabilité de la détection.
     * @param created_at La date de l'enregistrement.
     */
    public HistoryResult(Integer id, String plate_text, Float confidence, String created_at) {
        this.id = id;
        this.plate_text = plate_text;
        this.confidence = confidence;
        this.created_at = created_at;
    }

    /**
     * Récupère l'identifiant unique du résultat.
     *
     * @return L'ID sous forme d'{@link Integer}.
     */
    public Integer getId() { return id; }

    /**
     * Récupère le texte de la plaque d'immatriculation.
     *
     * @return Le texte sous forme de {@link String}.
     */
    public String getPlate_text() { return plate_text; }

    /**
     * Récupère le score de confiance de l'analyse.
     *
     * @return Le score sous forme de {@link Float}.
     */
    public Float getConfidence() { return confidence; }

    /**
     * Récupère la date de création de l'entrée.
     *
     * @return La date de création sous forme de {@link String}.
     */
    public String getCreated_at() { return created_at; }
}