package com.example.snaptaplaque.network;

import com.example.snaptaplaque.models.api.account.*;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionRequest;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.predictions.StatsResponse;
import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interface définissant les endpoints de l'API REST utilisée par l'application SnapTaPlaque.
 */
public interface ApiService {

    /********************/
    /* ACCOUNT ENDPOINT */
    /********************/

    /**
     * Endpoint pour l'authentification d'un utilisateur.
     *
     * @param loginRequest Un objet {@link LoginRequest} contenant les informations d'identification de l'utilisateur (nom d'utilisateur et mot de passe).
     * @return Un objet {@link Call} encapsulant la réponse de l'API, contenant un {@link LoginResponse} en cas de succès.
     */
    @FormUrlEncoded
    @POST("v1/account/login")
    Call<LoginResponse> login(
            @Body LoginRequest loginRequest
    );

    /**
     * Endpoint pour l'enregistrement d'un nouvel utilisateur.
     *
     * @param registerRequest Un objet {@link RegisterRequest} contenant les informations nécessaires à l'inscription (nom d'utilisateur, prénom, nom, mot de passe, email).
     * @return Un objet {@link Call} encapsulant la réponse de l'API, contenant un {@link RegisterResponse} en cas de succès.
     */
    @POST("v1/account/register")
    Call<RegisterResponse> register(
            @Body RegisterRequest registerRequest
    );

    @GET("v1/account/me")
    Call<MeResponse> me();

    @GET("v1/account/data-export")
    Call<DataExportResponse> data_export();

    @DELETE("v1/account/delete-account")
    Call<DeleteAccountResponse> delete_account();


    /************************/
    /* PREDICTIONS ENDPOINT */
    /************************/

    /**
     * Endpoint pour la prédiction de la plaque d'immatriculation à partir d'une image.
     *
     */
    @POST("v1/predictions/predict")
    Call<PredictionResponse> predict(
            @Body PredictionRequest predictionRequest
    );

    @GET("v1/predictions/history")
    Call<HistoryResponse> history();

    @GET("v1/predictions/stats")
    Call<StatsResponse> stats();

    /*********************/
    /* VEHICLES ENDPOINT */
    /*********************/
    @POST("v1/vehicles/info")
    Call<InfoResponse> vehicleInfo(
            @Body InfoRequest infoRequest
    );

    @GET("v1/vehicles/history")
    Call<HistoryResponse> historyVehicles();

    /*******************************/
    /* GLOBAL INFORMATION ENDPOINT */
    /*******************************/
    /**
     * Test si l'API est accessible en effectuant une requête GET sur un endpoint de test.
     */
    @GET("health")
    Call<HealthResponse> health();

    /**
     * Faire appel à l'endpoint privacy-policy qui renvoie comment l'api utilise
     * les données fournis et quelles sont les droits de l'utilisateur
     * (suppression, data-export, ...)
     */
    @GET("privacy-policy")
    Call<RgpdResponse> privacy_policy();

    @GET("versions")
    Call<ApiVersionResponse> versions();

    /******************/
    /* MODEL ENDPOINT */
    /******************/
    @GET("v1/model/info")
    Call<ModelInfoResponse> modelInfo();

    /**********************/
    /* FAVORITES ENDPOINT */
    /**********************/
    @POST("v1/favorites/add")
    Call<FavoritesAddResponse> add(
            @Body FavoritesAddRequest favoritesAddRequest
    );

    @DELETE("v1/favorites/remove")
    Call<FavoritesRemoveResponse> remove(
        @Body FavoritesRemoveRequest favoritesRemoveRequest
    );

    @GET("v1/favorites/all")
    Call<FavoriteAllResponse> all();

}
