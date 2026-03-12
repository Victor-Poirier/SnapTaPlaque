package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.response.ApiResponseFavorites;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

    /**
     *
     * @param favoritesAddRequest
     * @param apiCallback
     * @param apiResponseFavorites
     */
    public static void addFavorite(FavoritesAddRequest favoritesAddRequest, ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){
        apiService.add(favoritesAddRequest)
                .enqueue(new Callback<FavoritesAddResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesAddResponse> call, Response<FavoritesAddResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                            apiResponseFavorites.AddResponse(response.body());
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
     * @param apiResponseFavorites
     */
    public static void removeFavorite(FavoritesRemoveRequest favoritesRemoveRequest, ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){

        apiService.remove(favoritesRemoveRequest)
                .enqueue(new Callback<FavoritesRemoveResponse>() {
                    @Override
                    public void onResponse(Call<FavoritesRemoveResponse> call, Response<FavoritesRemoveResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                            apiResponseFavorites.RemoveResponse(response.body());
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
     * @param apiResponseFavorites
     */
    public static void allFavorites(ApiCallback apiCallback, ApiResponseFavorites apiResponseFavorites){
        apiService.all()
                .enqueue(new Callback<FavoriteAllResponse>() {
                    @Override
                    public void onResponse(Call<FavoriteAllResponse> call, Response<FavoriteAllResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                            apiResponseFavorites.AllResponse(response.body());
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
