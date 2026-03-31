package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour les informations relatives au modèle prédictif.
 * <p>Cette classe permet d'interroger le serveur pour obtenir des détails techniques
 * sur le modèle d'apprentissage automatique (IA) utilisé par l'application (version,
 * date de mise à jour, capacités de reconnaissance, etc.).</p>
 */
public class ModelCall {

    /** Instance du service API générée par le client Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    /**
     * Récupère les informations techniques du modèle de reconnaissance.
     * * <p>Cet appel est généralement asynchrone et ne nécessite pas d'authentification
     * utilisateur, permettant ainsi de vérifier la compatibilité du modèle avant
     * d'effectuer des prédictions.</p>
     *
     * @param apiCallback Callback de retour pour traiter la réponse {@link ModelInfoResponse}.
     */
    public static void modelInfo(ApiCallback apiCallback){
        apiService.modelInfo()
                .enqueue(new Callback<ModelInfoResponse>() {
                    /**
                     * Invoquée lorsque le serveur répond à la demande d'informations sur le modèle.
                     * @param call     L'objet représentant l'appel réseau effectué.
                     * @param response La réponse HTTP contenant les métadonnées du modèle.
                     */
                    @Override
                    public void onResponse(Call<ModelInfoResponse> call, Response<ModelInfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'échec de la communication réseau ou d'indisponibilité du serveur.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception ou l'erreur réseau rencontrée.
                     */
                    @Override
                    public void onFailure(Call<ModelInfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
