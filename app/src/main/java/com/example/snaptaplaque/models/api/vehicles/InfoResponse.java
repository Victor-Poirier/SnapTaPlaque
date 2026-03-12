package com.example.snaptaplaque.models.api.vehicles;

import com.example.snaptaplaque.models.Vehicle;

public class InfoResponse {

    private String license_plate;

    private String brand;

    private String model;

    private String info;

    private String energy;

    public InfoResponse(String license_plate, String brand, String model, String info, String energy) {
        this.license_plate = license_plate;
        this.brand = brand;
        this.model = model;
        this.info = info;
        this.energy = energy;
    }

    public Vehicle createVehicles(){
        return new Vehicle(this.getLicensePlate(), this.getBrand(), this.getModel(), this.getInfo(), this.getEnergy(), false);
    }

    public String getLicensePlate() { return license_plate; }

    public String getBrand() { return brand; }

    public String getModel() { return model; }

    public String getInfo() { return info; }

    public String getEnergy() { return energy; }

}
