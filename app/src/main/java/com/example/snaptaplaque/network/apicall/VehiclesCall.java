package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    public static void vehicleInfo(InfoRequest infoRequest, ApiCallback apiCallback){
        apiService.vehicleInfo(infoRequest)
                .enqueue(new Callback<InfoResponse>() {
                    @Override
                    public void onResponse(Call<InfoResponse> call, Response<InfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
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

    public static void history(ApiCallback apiCallback){
        apiService.historyVehicles()
                .enqueue(new Callback<HistoryResponse>() {
                    @Override
                    public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<HistoryResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

}
