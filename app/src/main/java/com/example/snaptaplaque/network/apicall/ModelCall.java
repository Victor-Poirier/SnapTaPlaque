package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiModelResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    public static void modelInfo(ApiCallback apiCallback, ApiModelResponse apiModelResponse){
        apiService.modelInfo()
                .enqueue(new Callback<ModelInfoResponse>() {
                    @Override
                    public void onResponse(Call<ModelInfoResponse> call, Response<ModelInfoResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiModelResponse.modelInfoResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ModelInfoResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
