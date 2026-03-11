package com.example.snaptaplaque.network.apiCall;

public interface ApiCallback {
    void onResponseSuccess(String message);
    void onResponseFailure(String message);
    void onCallFailure(Throwable t);
}
