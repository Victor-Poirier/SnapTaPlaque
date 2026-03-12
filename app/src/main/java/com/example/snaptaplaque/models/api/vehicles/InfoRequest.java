package com.example.snaptaplaque.models.api.vehicles;

public class InfoRequest {
    private String license_plate;

    public InfoRequest(String lp){ this.license_plate = lp; }

    public String getLicense_plate() {
        return license_plate;
    }
}
