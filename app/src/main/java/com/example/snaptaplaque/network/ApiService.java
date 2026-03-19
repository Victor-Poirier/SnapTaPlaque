package com.example.snaptaplaque.network;

import com.example.snaptaplaque.models.api.account.*;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.predictions.StatsResponse;
import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.RgpdRequest;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;
import com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interface définissant les endpoints de l'API REST utilisée par l'application SnapTaPlaque.
 */
public interface ApiService {

    public static int ERROR_TOKEN_EXPIRE = 401;

    /********************/
    /* ACCOUNT ENDPOINT */
    /********************/

    /**
     * Endpoint pour l'authentification d'un utilisateur.
     *
     * @return Un objet {@link Call} encapsulant la réponse de l'API, contenant un {@link LoginResponse} en cas de succès.
     */
    @FormUrlEncoded
    @POST("v1/account/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
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
    Call<MeResponse> me(
            @Header("Authorization") String token
    );

    @GET("v1/account/me/data-export")
    Call<DataExportResponse> data_export(
            @Header("Authorization") String token
    );

    @DELETE("v1/account/me/delete-account")
    Call<DeleteAccountResponse> delete_account(
            @Header("Authorization") String token
    );

    @GET("v1/account/me/profile-picture")
    Call<ResponseBody> profile_picture(
            @Header("Authorization") String token
    );

    @Multipart
    @POST("v1/account/me/change-profile-picture")
    Call<ChangeProfilePictureResponse> changeProfilePicture(
            @Header("Authorization") String token,
            @Part MultipartBody.Part filePart
    );

    @DELETE("v1/account/me/delete-profile-picture")
    Call<DeleteProfilePictureResponse> deleteProfilePicture(
            @Header("Authorization") String token
    );

    /************************/
    /* PREDICTIONS ENDPOINT */
    /************************/

    /**
     * Endpoint pour la prédiction de la plaque d'immatriculation à partir d'une image.
     *
     */
    @Multipart
    @POST("v1/predictions/predict")
    Call<PredictionResponse> predict(
            @Header("Authorization") String token,
            @Part MultipartBody.Part filePart
    );

    @GET("v1/predictions/history")
    Call<HistoryResponse> history(
            @Header("Authorization") String token
    );

    @GET("v1/predictions/stats")
    Call<StatsResponse> stats(
            @Header("Authorization") String token
    );

    /*********************/
    /* VEHICLES ENDPOINT */
    /*********************/
    @POST("v1/vehicles/info")
    Call<InfoResponse> vehicleInfo(
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );

    @GET("v1/vehicles/history")
    Call<HistoryVehiclesResponse> vehiclesHistory(
            @Header("Authorization") String token
    );

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
    @POST("privacy-policy")
    Call<RgpdResponse> privacy_policy(
            @Body RgpdRequest rgpdRequest
    );

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
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );

    @DELETE("v1/favorites/remove")
    Call<FavoritesRemoveResponse> remove(
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );

    @GET("v1/favorites/all")
    Call<FavoriteAllResponse> all(
            @Header("Authorization") String token
    );

}
