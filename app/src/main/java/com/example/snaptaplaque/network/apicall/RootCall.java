package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiRootResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RootCall {

    public static void apiVersion(ApiService apiService, ApiCallback apiCallback, ApiRootResponse apiRootResponse){
        apiService.versions()
                .enqueue(new Callback<ApiVersionResponse>() {
                    @Override
                    public void onResponse(Call<ApiVersionResponse> call, Response<ApiVersionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiRootResponse.apiVersionResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiVersionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    public static void privacyPolicy(ApiService apiService, ApiCallback apiCallback, ApiRootResponse apiRootResponse){
        apiService.privacy_policy()
                .enqueue(new Callback<RgpdResponse>() {
                    @Override
                    public void onResponse(Call<RgpdResponse> call, Response<RgpdResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiRootResponse.rgpdResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<RgpdResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
