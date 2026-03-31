package com.example.snaptaplaque.network.apicall;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;
import com.example.snaptaplaque.models.api.root.RgpdRequest;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour les fonctions de base et l'état du système (Root).
 * <p>Cette classe permet de vérifier la santé du serveur, d'obtenir les versions
 * de l'API disponibles et de récupérer les documents légaux liés au RGPD.</p>
 */
public class RootCall {

    /** Tag pour les logs de débogage des tests API. */
    private static final String TAG = "API_TEST";

    /** Durée maximale autorisée (5 secondes) pour la réponse du Health Check avant annulation. */
    private static final int API_TIMEOUT_MS = 5000; // 5 secondes
    /** Instance du service API générée par Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    /**
     * Récupère les informations sur les versions de l'API supportées.
     *
     * @param apiCallback Callback pour traiter la réponse {@link ApiVersionResponse}.
     */
    public static void apiVersion(ApiCallback apiCallback){
        apiService.versions()
                .enqueue(new Callback<ApiVersionResponse>() {
                    /**
                     * Invoquée lors de la réception de la liste des versions.
                     * @param call     L'appel réseau.
                     * @param response La réponse HTTP contenant les versions de l'API.
                     */
                    @Override
                    public void onResponse(Call<ApiVersionResponse> call, Response<ApiVersionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur de récupération des versions.
                     */
                    @Override
                    public void onFailure(Call<ApiVersionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Récupère la politique de confidentialité (RGPD) dans la langue spécifiée.
     *
     * @param apiCallback Callback pour traiter le texte de la politique via {@link RgpdResponse}.
     * @param language    Code langue souhaité (ex: "fr", "en").
     */
    public static void privacyPolicy(ApiCallback apiCallback, String language){
        RgpdRequest request = new RgpdRequest(language);
        apiService.privacy_policy(request)
                .enqueue(new Callback<RgpdResponse>() {
                    /**
                     * Invoquée lorsque le serveur renvoie le contenu légal.
                     * @param call     L'appel réseau.
                     * @param response La réponse HTTP contenant le texte RGPD.
                     */
                    @Override
                    public void onResponse(Call<RgpdResponse> call, Response<RgpdResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'échec de récupération des documents légaux.
                     */
                    @Override
                    public void onFailure(Call<RgpdResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Vérifie l'état de santé (Health Check) du serveur avec un timeout personnalisé.
     * <p>Cette méthode utilise un {@link Handler} pour annuler la requête si le serveur
     * ne répond pas dans les 5 secondes, garantissant que l'application ne reste pas
     * bloquée en attente d'un service injoignable.</p>
     *
     * @param apiCallback Callback pour notifier si le système est opérationnel ou non.
     */
    public static void health(ApiCallback apiCallback){

        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            apiService.health().cancel();
            Log.e(TAG, "API timeout after " + API_TIMEOUT_MS + "ms");
        };
        timeoutHandler.postDelayed(timeoutRunnable, API_TIMEOUT_MS);

        apiService.health()
                .enqueue(new Callback<HealthResponse>() {
                    /**
                     * Invoquée lorsque le serveur répond sur son état de santé.
                     * <p>Le timeout est annulé dès réception de la réponse.</p>
                     * @param call     L'appel réseau.
                     * @param response La réponse HTTP (Statut du serveur).
                     */
                    @Override
                    public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur réseau ou d'annulation manuelle (timeout).
                     */
                    @Override
                    public void onFailure(Call<HealthResponse> call, Throwable t) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
