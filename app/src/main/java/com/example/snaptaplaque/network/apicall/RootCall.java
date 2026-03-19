package com.example.snaptaplaque.network.apicall;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;
import com.example.snaptaplaque.models.api.root.RgpdRequest;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RootCall {

    private static final String TAG = "API_TEST";

    /** Durée de 5 secondes pour le timeout de la connexion à l'API */
    private static final int API_TIMEOUT_MS = 5000; // 5 secondes

    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    public static void apiVersion(ApiCallback apiCallback){
        apiService.versions()
                .enqueue(new Callback<ApiVersionResponse>() {
                    @Override
                    public void onResponse(Call<ApiVersionResponse> call, Response<ApiVersionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiVersionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    public static void privacyPolicy(ApiCallback apiCallback, String language){
        RgpdRequest request = new RgpdRequest(language);
        apiService.privacy_policy(request)
                .enqueue(new Callback<RgpdResponse>() {
                    @Override
                    public void onResponse(Call<RgpdResponse> call, Response<RgpdResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<RgpdResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    public static void health(ApiCallback apiCallback){

        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            apiService.health().cancel();
            Log.e(TAG, "API timeout after " + API_TIMEOUT_MS + "ms");
        };
        timeoutHandler.postDelayed(timeoutRunnable, API_TIMEOUT_MS);

        apiService.health()
                .enqueue(new Callback<HealthResponse>() {
                    @Override
                    public void onResponse(Call<HealthResponse> call, Response<HealthResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            timeoutHandler.removeCallbacks(timeoutRunnable);
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<HealthResponse> call, Throwable t) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
