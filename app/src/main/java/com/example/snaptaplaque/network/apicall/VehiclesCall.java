package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiVehiclesResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    // Endpoint : /v1/vehicles/info
    public static void getVehicleInfo(InfoRequest infoRequest, ApiCallback apiCallback, ApiVehiclesResponse apiVehiclesResponse){
        apiService.vehicleInfo(infoRequest)
                .enqueue(new Callback<InfoResponse>() {
                    @Override
                    public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                            apiVehiclesResponse.infoResponse(response.body());
                        }
                        else {
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
