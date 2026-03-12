package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    /**
     * Récupère les informations d'un véhicule par sa plaque d'immatriculation.
     * Vérifie d'abord si le véhicule est déjà dans le SharedViewModel avant d'appeler l'API.
     *
     * @param plate           la plaque d'immatriculation à rechercher
     * @param sharedViewModel le ViewModel partagé pour vérifier le cache
     * @param apiCallback     le callback pour gérer la réponse
     */
    public static void getInfo(String plate, SharedViewModel sharedViewModel, ApiCallback apiCallback) {
        // Vérifier si le véhicule est déjà dans le SharedViewModel
        if (sharedViewModel != null) {
            List<Vehicle> vehicles = sharedViewModel.getVehicleList().getValue();
            if (vehicles != null) {
                for (Vehicle v : vehicles) {
                    if (v.getImmatriculation().equalsIgnoreCase(plate)) {
                        // Véhicule déjà en cache, pas besoin d'appeler l'API
                        InfoResponse cachedResponse = new InfoResponse(
                                v.getImmatriculation(),
                                v.getBrand(),
                                v.getModel(),
                                v.getInfo(),
                                v.getEnergy()
                        );
                        apiCallback.onResponseSuccess(Response.success(cachedResponse));
                        return;
                    }
                }
            }
        }

        // Appel à l'API si le véhicule n'est pas en cache
        apiService.vehicleInfo(plate)
                .enqueue(new Callback<InfoResponse>() {
                    @Override
                    public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            apiCallback.onResponseSuccess(response);
                        } else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<InfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
