package com.example.snaptaplaque.network.apicall;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;

import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    private static SessionManager sessionManager = AccountCall.sessionManager;
    /**
     *
     * @param favoritesAddRequest
     * @param apiCallback
     */
    public static void addFavorite(FavoritesAddRequest favoritesAddRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.add("Bearer " + token, favoritesAddRequest.getLicensePlate())
                .enqueue(new Callback<FavoritesAddResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesAddResponse> call, Response<FavoritesAddResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoritesAddResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     *
     * @param favoritesRemoveRequest
     * @param apiCallback
     */
    public static void removeFavorite(FavoritesRemoveRequest favoritesRemoveRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.remove("Bearer " + token, favoritesRemoveRequest.getLicensePlate())
                .enqueue(new Callback<FavoritesRemoveResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesRemoveResponse> call, Response<FavoritesRemoveResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoritesRemoveResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     *
     * @param apiCallback
     */
    public static void allFavorites(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.all("Bearer " + token)
                .enqueue(new Callback<FavoriteAllResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteAllResponse> call, Response<FavoriteAllResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoriteAllResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
