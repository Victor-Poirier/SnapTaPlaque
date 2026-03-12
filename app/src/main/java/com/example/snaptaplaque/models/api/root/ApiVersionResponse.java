package com.example.snaptaplaque.models.api.root;

import java.util.List;

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
}
