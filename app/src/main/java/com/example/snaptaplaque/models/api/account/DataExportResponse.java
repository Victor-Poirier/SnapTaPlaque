package com.example.snaptaplaque.models.api.account;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;

import java.util.List;

public class DataExportResponse {
    private MeResponse profile;
    private List<PredictionResponse>  predictions;
    private FavoriteAllResponse favorites;

    public MeResponse getProfile() {
        return profile;
    }

    public List<PredictionResponse> getPredictions() {
        return predictions;
    }

    public FavoriteAllResponse getFavorites() {
        return favorites;
    }

    public DataExportResponse(MeResponse profile, List<PredictionResponse> predictions, FavoriteAllResponse favorites) {
        this.profile = profile;
        this.predictions = predictions;
        this.favorites = favorites;
    }
}
