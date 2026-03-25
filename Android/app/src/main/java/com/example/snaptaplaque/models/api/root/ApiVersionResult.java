package com.example.snaptaplaque.models.api.root;

import android.content.Context;

import com.example.snaptaplaque.R;

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

    public String createString(Context context){
        return  context.getString(R.string.api_version_result_version) + getVersion() + "\n" +
                context.getString(R.string.api_version_result_statut) + getStatus() + "\n" +
                        context.getString(R.string.api_version_result_pipeline) + getPipeline() + "\n";
    }
}
