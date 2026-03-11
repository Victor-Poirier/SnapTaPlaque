package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiResponseFavorites;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesCall {
    // Endpoint : /v1/favorites/add
    public static void addFavorite(ApiService apiService, FavoritesAddRequest favoritesAddRequest, ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){
        apiService.add(favoritesAddRequest)
                .enqueue(new Callback<FavoritesAddResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesAddResponse> call, Response<FavoritesAddResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiResponseFavorites.AddResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoritesAddResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    // Endpoint : /v1/favorites/remove
    public static void removeFavorite(ApiService apiService, FavoritesRemoveRequest favoritesRemoveRequest, ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){

        apiService.remove(favoritesRemoveRequest)
                .enqueue(new Callback<FavoritesRemoveResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesRemoveResponse> call, Response<FavoritesRemoveResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiResponseFavorites.RemoveResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoritesRemoveResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    public static void allFavorites(ApiService apiService, ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){
        apiService.all()
                .enqueue(new Callback<FavoriteAllResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteAllResponse> call, Response<FavoriteAllResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response.message());
                            apiResponseFavorites.AllResponse(response.body());
                        }
                        else {
                            apiCallback.onResponseFailure(response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<FavoriteAllResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
