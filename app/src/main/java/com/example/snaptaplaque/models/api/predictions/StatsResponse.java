package com.example.snaptaplaque.models.api.predictions;

public class StatsResponse {
    private Integer total_predictions;

    public StatsResponse(Integer total_predictions) {
        this.total_predictions = total_predictions;
    }

    public Integer getTotal_predictions() {
        return total_predictions;
    }
}
