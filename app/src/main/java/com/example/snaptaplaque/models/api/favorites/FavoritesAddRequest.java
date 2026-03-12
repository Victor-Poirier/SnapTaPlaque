package com.example.snaptaplaque.models.api.favorites;

import com.google.gson.annotations.SerializedName;

public class FavoritesAddRequest {
    @SerializedName("license_plate")
    private String licensePlate;

    public FavoritesAddRequest(String lp) {this.licensePlate = lp; }

    public String getLicensePlate() {
        return licensePlate;
    }
}
