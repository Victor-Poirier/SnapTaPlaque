package com.example.snaptaplaque.network.apicall;

import android.util.Log;

import com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour la récupération des données techniques des véhicules.
 * <p>Cette classe permet d'interroger les bases de données d'immatriculation pour obtenir
 * les détails d'un véhicule spécifique (marque, modèle, motorisation) et de consulter
 * l'historique des véhicules consultés par l'utilisateur.</p>
 */
public class VehiclesCall {
    /** Instance du service API configurée via Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    /** Récupération du gestionnaire de session pour l'authentification Bearer. */
    private static SessionManager sessionManager = AccountCall.sessionManager;

    /**
     * Récupère les informations détaillées d'un véhicule via sa plaque d'immatriculation.
     * * <p>Cette méthode effectue une requête sécurisée. Si le serveur renvoie une erreur
     * indiquant que le jeton est expiré, une déconnexion automatique est déclenchée.</p>
     *
     * @param infoRequest Objet contenant le numéro de plaque d'immatriculation cible.
     * @param apiCallback Callback pour traiter la fiche technique {@link InfoResponse}.
     */
    public static void vehicleInfo(InfoRequest infoRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.vehicleInfo("Bearer " + token, infoRequest.getLicense_plate())
                .enqueue(new Callback<InfoResponse>() {
                    /**
                     * Invoquée lorsque le serveur répond à la demande d'informations véhicule.
                     * @param call     L'appel réseau effectué.
                     * @param response La réponse contenant les détails techniques ou une erreur.
                     */
                    @Override
                    public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            Log.e("Favorites", String.valueOf(response.code()));
                            if (response.code() == ApiService.ERROR_TOKEN_EXPIRE){
                                sessionManager.logout();
                            }
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas de coupure réseau ou d'impossibilité d'atteindre le serveur.
                     */
                    @Override
                    public void onFailure(Call<InfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Récupère la liste historique des véhicules précédemment consultés par l'utilisateur.
     *
     * @param apiCallback Callback pour transmettre l'objet {@link HistoryVehiclesResponse}.
     */
    public static void vehiclesHistory(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.vehiclesHistory("Bearer " + token)
                .enqueue(new Callback<HistoryVehiclesResponse>() {
                    /**
                     * Invoquée lors de la réception de la liste historique des véhicules.
                     * @param call     L'appel réseau.
                     * @param response La response HTTP contenant la collection de véhicules.
                     */
                    @Override
                    public void onResponse(Call<HistoryVehiclesResponse> call, Response<HistoryVehiclesResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'échec technique du chargement de l'historique.
                     */
                    @Override
                    public void onFailure(Call<HistoryVehiclesResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
