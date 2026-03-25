package com.example.snaptaplaque.network.apicall;

import retrofit2.Response;

public interface ApiCallback {
    void onResponseSuccess(Response response);
    void onResponseFailure(Response response);
    void onCallFailure(Throwable t);
}
