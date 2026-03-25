package com.example.snaptaplaque.models.api.vehicles;

import java.util.List;

public class HistoryVehiclesResponse {
    private List<InfoResponse> history;

    public HistoryVehiclesResponse(List<InfoResponse> history) {
        this.history = history;
    }

    public List<InfoResponse> getHistory() {
        return history;
    }
}
