package com.example.snaptaplaque.models.api.model;

public class ModelInfoResponse {
    private boolean loaded;
    private String model_type;
    private String pipeline;

    public boolean isLoaded() {
        return loaded;
    }

    public String getPipeline() {
        return pipeline;
    }

    public String getModel_type() {
        return model_type;
    }
}
