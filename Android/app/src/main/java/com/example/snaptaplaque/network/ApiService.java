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
 * Interface définissant les points d'entrée (endpoints) de l'API REST SnapTaPlaque.
 * <p>Cette interface est utilisée par Retrofit pour générer les requêtes HTTP.
 * Elle regroupe les services d'authentification, de gestion de profil,
 * de prédiction d'IA, de gestion des véhicules et des favoris.</p>
 */
public interface ApiService {

    /** Code d'erreur HTTP retourné lorsque le jeton JWT n'est plus valide. */
    public static int ERROR_TOKEN_EXPIRE = 401;

    /********************/
    /* ACCOUNT ENDPOINT */
    /********************/

    /**
     * Authentifie un utilisateur via son pseudonyme et son mot de passe.
     * @param username Nom d'utilisateur.
     * @param password Mot de passe.
     * @return Call pour obtenir le token d'accès.
     */
    @FormUrlEncoded
    @POST("v1/account/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    /**
     * Crée un nouveau compte utilisateur.
     * @param registerRequest Objet contenant les données d'inscription.
     * @return Call confirmant la création du compte.
     */
    @POST("v1/account/register")
    Call<RegisterResponse> register(
            @Body RegisterRequest registerRequest
    );

    /**
     * Récupère les informations personnelles du profil connecté.
     * @param token Jeton d'authentification (Bearer).
     */
    @GET("v1/account/me")
    Call<MeResponse> me(
            @Header("Authorization") String token
    );

    /**
     * Demande l'exportation des données personnelles (RGPD).
     * @param token Jeton d'authentification.
     */
    @GET("v1/account/me/data-export")
    Call<DataExportResponse> data_export(
            @Header("Authorization") String token
    );

    /**
     * Supprime définitivement le compte de l'utilisateur.
     * @param token Jeton d'authentification.
     */
    @DELETE("v1/account/me/delete-account")
    Call<DeleteAccountResponse> delete_account(
            @Header("Authorization") String token
    );

    /**
     * Télécharge l'image de profil sous forme de flux binaire.
     * @param token Jeton d'authentification.
     */
    @GET("v1/account/me/profile-picture")
    Call<ResponseBody> profile_picture(
            @Header("Authorization") String token
    );

    /**
     * Met à jour la photo de profil via un envoi de fichier.
     * @param token Jeton d'authentification.
     * @param filePart Partie multipart contenant le fichier image.
     */
    @Multipart
    @POST("v1/account/me/change-profile-picture")
    Call<ChangeProfilePictureResponse> changeProfilePicture(
            @Header("Authorization") String token,
            @Part MultipartBody.Part filePart
    );

    /**
     * Supprime la photo de profil actuelle.
     * @param token Jeton d'authentification.
     */
    @DELETE("v1/account/me/delete-profile-picture")
    Call<DeleteProfilePictureResponse> deleteProfilePicture(
            @Header("Authorization") String token
    );

    /************************/
    /* PREDICTIONS ENDPOINT */
    /************************/

    /**
     * Soumet une image pour analyse et reconnaissance de plaque.
     * @param token Jeton d'authentification.
     * @param filePart Image du véhicule/plaque.
     */
    @Multipart
    @POST("v1/predictions/predict")
    Call<PredictionResponse> predict(
            @Header("Authorization") String token,
            @Part MultipartBody.Part filePart
    );

    /**
     * Récupère l'historique des analyses effectuées.
     */
    @GET("v1/predictions/history")
    Call<HistoryResponse> history(
            @Header("Authorization") String token
    );

    /**
     * Récupère les statistiques d'analyse de l'utilisateur.
     */
    @GET("v1/predictions/stats")
    Call<StatsResponse> stats(
            @Header("Authorization") String token
    );

    /*********************/
    /* VEHICLES ENDPOINT */
    /*********************/

    /**
     * Récupère la fiche technique d'un véhicule à partir de sa plaque.
     * @param licensePlate Numéro d'immatriculation.
     */
    @POST("v1/vehicles/info")
    Call<InfoResponse> vehicleInfo(
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );

    /**
     * Récupère la liste des véhicules consultés par l'utilisateur.
     */
    @GET("v1/vehicles/history")
    Call<HistoryVehiclesResponse> vehiclesHistory(
            @Header("Authorization") String token
    );

    /*******************************/
    /* GLOBAL INFORMATION ENDPOINT */
    /*******************************/

    /**
     * Vérifie la disponibilité du serveur (Health Check).
     */
    @GET("health")
    Call<HealthResponse> health();

    /**
     * Récupère la politique de confidentialité RGPD.
     * @param rgpdRequest Requête incluant la langue souhaitée.
     */
    @POST("privacy-policy")
    Call<RgpdResponse> privacy_policy(
            @Body RgpdRequest rgpdRequest
    );

    /**
     * Liste les versions disponibles de l'API.
     */
    @GET("versions")
    Call<ApiVersionResponse> versions();

    /******************/
    /* MODEL ENDPOINT */
    /******************/

    /**
     * Récupère les métadonnées sur le modèle de prédiction utilisé.
     */
    @GET("v1/model/info")
    Call<ModelInfoResponse> modelInfo();

    /**********************/
    /* FAVORITES ENDPOINT */
    /**********************/

    /**
     * Ajoute un véhicule aux favoris.
     * @param licensePlate Plaque du véhicule à marquer.
     */
    @POST("v1/favorites/add")
    Call<FavoritesAddResponse> add(
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );
    /**
     * Retire un véhicule des favoris.
     * @param licensePlate Plaque du véhicule à retirer.
     */
    @DELETE("v1/favorites/remove")
    Call<FavoritesRemoveResponse> remove(
            @Header("Authorization") String token,
            @Query("license_plate") String licensePlate
    );

    /**
     * Liste tous les favoris de l'utilisateur.
     */
    @GET("v1/favorites/all")
    Call<FavoriteAllResponse> all(
            @Header("Authorization") String token
    );
}