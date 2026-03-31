package com.example.snaptaplaque.models.api.root;

/**
 * Modèle de données représentant la réponse du "Health Check" (bilan de santé) du backend.
 * <p>Cette classe est utilisée pour valider que le serveur distant est en ligne et
 * fonctionnel. Elle fournit une confirmation rapide du statut opérationnel et
 * de la version majeure de l'API déployée.</p>
 * <p>Exemple de structure JSON correspondante :</p>
 * <pre>
 * {
 * "status": "healthy",
 * "version": "1.0"
 * }
 * </pre>
 */
public class HealthResponse {

    /** L'état de santé du serveur (ex: "healthy", "degraded", "maintenance"). */
    private String status;

    /** La version actuelle de l'API (ex: "1.0"). */
    private String version;

    /**
     * Constructeur pour initialiser la réponse de santé.
     *
     * @param status  Le statut actuel du serveur.
     * @param version La version logicielle du backend.
     */
    public HealthResponse(String status, String version) {
        this.status = status;
        this.version = version;
    }

    /**
     * Récupère le statut opérationnel du serveur.
     *
     * @return Le statut sous forme de {@link String}. Généralement "healthy" si tout va bien.
     */
    public String getStatus() { return status; }

    /**
     * Récupère la version du backend.
     *
     * @return Le numéro de version sous forme de {@link String}.
     */
    public String getVersion() {
        return version;
    }
}
