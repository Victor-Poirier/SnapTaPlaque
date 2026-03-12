package com.example.snaptaplaque.models.api.favorites;

import com.google.gson.annotations.SerializedName;

public class FavoritesRemoveRequest {

    private String licensePlate;

    public FavoritesRemoveRequest(String lp) {this.licensePlate = lp; }

    public String getLicensePlate() {
        return licensePlate;
    }
}
