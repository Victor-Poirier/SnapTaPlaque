package com.example.snaptaplaque.network.apicall;

public interface ApiCallback {
    void onResponseSuccess(String message);
    void onResponseFailure(String message);
    void onCallFailure(Throwable t);
}
