package com.example.snaptaplaque.models.api.predictions;

public class HistoryResult {
    private Integer id;
    private String plate_text;
    private Float confidence;
    private String created_at;

    public HistoryResult(Integer id, String plate_text, Float confidence, String created_at) {
        this.id = id;
        this.plate_text = plate_text;
        this.confidence = confidence;
        this.created_at = created_at;
    }

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
