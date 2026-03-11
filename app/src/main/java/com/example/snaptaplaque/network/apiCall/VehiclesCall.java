package com.example.snaptaplaque.network.apiCall;

import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apiCall.response.ApiVehiclesResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesCall {

    // Endpoint : /v1/vehicles/info
    public static void getVehicleInfo(ApiService apiService, InfoRequest infoRequest, ApiCallback apiCallback, ApiVehiclesResponse apiVehiclesResponse){
        apiService.info(infoRequest)
                .enqueue(new Callback<InfoResponse>() {
                    @Override
                    public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiVehiclesResponse.infoResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<InfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

}
