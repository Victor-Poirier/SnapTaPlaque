package com.example.snaptaplaque.models.api.account;

import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import java.util.List;

public class DataExportResponse {
    private MeResponse profile;
    private List<PredictionResponse>  predictions;
    private List<InfoResponse> favorites;

    public MeResponse getProfile() {
        return profile;
    }

    public List<PredictionResponse> getPredictions() {
        return predictions;
    }

    public List<InfoResponse> getFavorites() {
        return favorites;
    }

    public DataExportResponse(MeResponse profile, List<PredictionResponse> predictions, List<InfoResponse> favorites) {
        this.profile = profile;
        this.predictions = predictions;
        this.favorites = favorites;
    }
}
