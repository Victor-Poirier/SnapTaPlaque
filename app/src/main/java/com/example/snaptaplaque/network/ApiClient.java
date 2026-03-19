package com.example.snaptaplaque.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    // private static final String BASE_URL = "https://danny-nonpresumptive-jadedly.ngrok-free.dev";
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
