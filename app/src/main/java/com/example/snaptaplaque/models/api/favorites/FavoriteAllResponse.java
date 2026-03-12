package com.example.snaptaplaque.models.api.favorites;

import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import java.util.List;

public class FavoriteAllResponse {

    private List<InfoResponse> favorites;

    public FavoriteAllResponse(List<InfoResponse> favorites) {
        this.favorites = favorites;
    }

    public List<InfoResponse> getFavorites() {
        return favorites;
    }
}
