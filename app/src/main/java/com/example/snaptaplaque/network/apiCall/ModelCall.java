package com.example.snaptaplaque.network.apiCall;

import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apiCall.response.ApiModelResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModelCall {
    public static void modelInfo(ApiService apiService, ApiCallback apiCallback, ApiModelResponse apiModelResponse){
        apiService.info()
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
