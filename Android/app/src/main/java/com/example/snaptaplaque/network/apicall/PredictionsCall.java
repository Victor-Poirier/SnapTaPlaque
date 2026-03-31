package com.example.snaptaplaque.network.apicall;


import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.predictions.StatsResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour le moteur de prédiction et l'historique des scans.
 * * <p>Cette classe orchestre les interactions avec les services d'IA de SnapTaPlaque.
 * Elle permet d'envoyer des images pour analyse, de récupérer les statistiques
 * d'utilisation et de consulter l'historique chronologique des prédictions effectuées.</p>
 */
public class PredictionsCall {
    /** Instance du service API générée par Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    /** Gestionnaire de session partagé pour l'authentification des requêtes. */
    private static SessionManager sessionManager = AccountCall.sessionManager;

    /**
     * Récupère l'historique des prédictions effectuées par l'utilisateur.
     *
     * @param apiCallback Callback pour transmettre la liste d'historique {@link HistoryResponse}.
     */
    public static void getHistory(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.history("Bearer " + token)
                .enqueue(new Callback<HistoryResponse>() {
                    /**
                     * Invoquée lors de la réception de la liste d'historique.
                     * @param call     L'appel réseau effectué.
                     * @param response La réponse HTTP contenant les entrées d'historique.
                     */
                    @Override
                    public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur de communication ou d'indisponibilité du service d'historique.
                     */
                    @Override
                    public void onFailure(Call<HistoryResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Récupère les statistiques d'utilisation des prédictions de l'utilisateur.
     *
     * @param apiCallback Callback pour traiter l'objet de statistiques {@link StatsResponse}.
     */
    public static void userStat(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.stats("Bearer " + token)
                .enqueue(new Callback<StatsResponse>() {
                    /**
                     * Invoquée lors de la réception des calculs de statistiques.
                     * @param call     L'appel réseau.
                     * @param response La réponse contenant les compteurs et données statistiques.
                     */
                    @Override
                    public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'échec de récupération des données statistiques.
                     */
                    @Override
                    public void onFailure(Call<StatsResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Envoie une image au serveur pour effectuer une prédiction (OCR et reconnaissance de plaque).
     *
     * @param filePart    Le fichier image capturé préparé en tant que partie Multipart.
     * @param apiCallback Callback pour traiter le résultat de l'analyse {@link PredictionResponse}.
     */
    public static void picturePredict(MultipartBody.Part filePart, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.predict("Bearer " + token, filePart)
                .enqueue(new Callback<PredictionResponse>() {
                    /**
                     * Invoquée lorsque le moteur d'IA a terminé l'analyse de l'image.
                     * @param call     L'appel réseau d'analyse.
                     * @param response La réponse contenant le numéro de plaque et les détails identifiés.
                     */
                    @Override
                    public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'interruption du transfert d'image ou de timeout serveur.
                     * <p>Note : Le transfert d'images étant plus lourd qu'une requête texte,
                     * cette méthode est sensible à la qualité de la connexion réseau.</p>
                     */
                    @Override
                    public void onFailure(Call<PredictionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
