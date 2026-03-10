package com.example.snaptaplaque.models.api.predictions;

public class HistoryResult {
    private Integer id;
    private String plate_text;
    private Float confidence;
    private String created_at;

    public Integer getId() {
        return id;
    }

    public String getPlate_text() {
        return plate_text;
    }

    public Float getConfidence() {
        return confidence;
    }

    public String getCreated_at() {
        return created_at;
    }
}
