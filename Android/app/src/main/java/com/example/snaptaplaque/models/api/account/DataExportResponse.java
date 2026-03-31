package com.example.snaptaplaque.models.api.account;

import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import java.util.List;

/**
 * Modèle de données représentant la structure complète d'un export de données utilisateur.
 * <p>Cette classe agrège l'ensemble des informations liées à un compte pour permettre
 * une portabilité des données ou une sauvegarde locale. Elle combine les données
 * personnelles, l'historique des scans (prédictions) et la liste des favoris.</p>
 */
public class DataExportResponse {

    /** Informations détaillées du profil utilisateur. */
    private MeResponse profile;
    /** Liste de l'historique des prédictions de plaques effectuées par l'utilisateur. */
    private List<PredictionResponse>  predictions;
    /** Liste des informations techniques des véhicules enregistrés en favoris. */
    private List<InfoResponse> favorites;

    /**
     * Récupère les données du profil utilisateur.
     *
     * @return Un objet {@link MeResponse} contenant les détails du compte.
     */
    public MeResponse getProfile() {
        return profile;
    }

    /**
     * Récupère l'historique des prédictions.
     *
     * @return Une liste d'objets {@link PredictionResponse}.
     */
    public List<PredictionResponse> getPredictions() {
        return predictions;
    }

    /**
     * Récupère la liste des véhicules favoris.
     *
     * @return Une liste d'objets {@link InfoResponse} correspondant aux favoris.
     */
    public List<InfoResponse> getFavorites() {
        return favorites;
    }

    /**
     * Constructeur complet pour initialiser l'objet d'export de données.
     *
     * @param profile     L'objet contenant les informations de base du profil.
     * @param predictions La liste des résultats d'analyses (OCR/Détection) passées.
     * @param favorites   La liste des véhicules sauvegardés par l'utilisateur.
     */
    public DataExportResponse(MeResponse profile, List<PredictionResponse> predictions, List<InfoResponse> favorites) {
        this.profile = profile;
        this.predictions = predictions;
        this.favorites = favorites;
    }
}
