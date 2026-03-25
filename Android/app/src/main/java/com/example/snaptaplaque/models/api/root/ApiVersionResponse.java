package com.example.snaptaplaque.models.api.root;

import android.content.Context;

import com.example.snaptaplaque.R;

public class ApiVersionResponse {
    private ApiVersionResult versions;

    private String latest;

    public ApiVersionResponse(ApiVersionResult versions, String latest) {
        this.versions = versions;
        this.latest = latest;
    }

    public String getLatest() {
        return latest;
    }

    public ApiVersionResult getVersions() {
        return versions;
    }

    public String createString(Context context){
        return  getVersions().createString(context) + "\n" +
                context.getString(R.string.api_version_response_latest) + getLatest();

    }
}
