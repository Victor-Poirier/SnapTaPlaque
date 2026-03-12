package com.example.snaptaplaque.network.apicall.response;

import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;

public abstract class ApiRootResponse {
    public void apiVersionResponse(ApiVersionResponse apiVersionResponse) {

    }

    public void rgpdResponse(RgpdResponse rgpdResponse) {

    }

    public void testApiResponse(HealthResponse testApiResponse) {

    }

    public void healthResponse(HealthResponse healthResponse){

    }
}
