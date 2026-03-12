package com.example.snaptaplaque.models.api.root;

public class ApiVersionResult {
    private String version;
    private String status;
    private String pipeline;

    public ApiVersionResult(String version, String pipeline, String status) {
        this.version = version;
        this.pipeline = pipeline;
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public String getPipeline() {
        return pipeline;
    }
}
