package com.example.snaptaplaque.network.apicall;

import android.util.Log;

import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    private static SessionManager sessionManager = AccountCall.sessionManager;

    /**
     * Récupère les informations d'un véhicule par sa plaque d'immatriculation.
     * Vérifie d'abord si le véhicule est déjà dans le SharedViewModel avant d'appeler l'API.
     *
     * @param apiCallback     le callback pour gérer la réponse
     */
    public static void vehicleInfo(InfoRequest infoRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.vehicleInfo("Bearer " + token, infoRequest.getLicense_plate())
                .enqueue(new Callback<InfoResponse>() {
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

                    @Override
                    public void onFailure(Call<InfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    public static void vehiclesHistory(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.vehiclesHistory("Bearer " + token)
                .enqueue(new Callback<HistoryVehiclesResponse>() {
                    @Override
                    public void onResponse(Call<HistoryVehiclesResponse> call, Response<HistoryVehiclesResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<HistoryVehiclesResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }


}
