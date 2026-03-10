package com.example.snaptaplaque.models.api.root;

import java.util.List;

public class ApiVersionResponse {
    private ApiVersionResult versions;

    private String latest;

    public String getLatest() {
        return latest;
    }

    public ApiVersionResult getVersions() {
        return versions;
    }
}
