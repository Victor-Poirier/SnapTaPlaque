package com.example.snaptaplaque.models.api.model;

import android.content.Context;

import com.example.snaptaplaque.R;

public class ModelInfoResponse {
    private boolean loaded;
    private String model_type;
    private String pipeline;

    public ModelInfoResponse(boolean loaded, String pipeline, String model_type) {
        this.loaded = loaded;
        this.pipeline = pipeline;
        this.model_type = model_type;
    }

    public String createString(Context context){
        return  context.getString(R.string.model_type) + model_type + "\n" +
                context.getString(R.string.pipeline)   +  pipeline  + "\n" +
                context.getString(R.string.is_loaded)  + loaded + "\n" ;
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
