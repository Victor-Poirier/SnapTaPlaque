package com.example.snaptaplaque.models.api.model;

public class ModelInfoResponse {
    private boolean loaded;
    private String model_type;
    private String pipeline;

    public ModelInfoResponse(boolean loaded, String pipeline, String model_type) {
        this.loaded = loaded;
        this.pipeline = pipeline;
        this.model_type = model_type;
    }

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
