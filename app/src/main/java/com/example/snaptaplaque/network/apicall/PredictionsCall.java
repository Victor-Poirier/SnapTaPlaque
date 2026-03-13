package com.example.snaptaplaque.network.apicall;

import android.util.Log;

import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.predictions.StatsResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionsCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    private static SessionManager sessionManager = AccountCall.sessionManager;

    // Endpoint : /v1/predictions/history
    public static void getHistory(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.history("Bearer " + token)
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

    // Endpoint : /v1/predictions/stats
    public static void userStat(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.stats("Bearer " + token)
                .enqueue(new Callback<StatsResponse>() {
                    @Override
                    public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<StatsResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    // Endpoint : /v1/predictions/predict
    public static void picturePredict(MultipartBody.Part filePart, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.predict("Bearer " + token, filePart)
                .enqueue(new Callback<PredictionResponse>() {
                    @Override
                    public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<PredictionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
