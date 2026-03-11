package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionRequest;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.predictions.StatsResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiPredictionsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionsCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    // Endpoint : /v1/predictions/history
    public static void getHistory(ApiCallback apiCallback, ApiPredictionsResponse apiPredictionsResponse){
        apiService.history()
                .enqueue(new Callback<HistoryResponse>() {
                    @Override
                    public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiPredictionsResponse.historyResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<HistoryResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    // Endpoint : /v1/predictions/stats
    public static void userStat(ApiCallback apiCallback, ApiPredictionsResponse apiPredictionsResponse){
        apiService.stats()
                .enqueue(new Callback<StatsResponse>() {
                    @Override
                    public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiPredictionsResponse.statsResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<StatsResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    // Endpoint : /v1/predictions/predict
    public void picturePredict(PredictionRequest predictionRequest, ApiCallback apiCallback, ApiPredictionsResponse apiPredictionsResponse){
        apiService.predict(predictionRequest)
                .enqueue(new Callback<PredictionResponse>() {
                    @Override
                    public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiPredictionsResponse.predictionResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<PredictionResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
