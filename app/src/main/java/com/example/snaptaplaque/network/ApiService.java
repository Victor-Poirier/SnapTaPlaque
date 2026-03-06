package com.example.snaptaplaque.network;

import com.example.snaptaplaque.models.api.*;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interface définissant les endpoints de l'API REST utilisée par l'application SnapTaPlaque.
 */
public interface ApiService {

    /**
     * Endpoint pour l'authentification d'un utilisateur.
     *
     * @param username Le nom d'utilisateur de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
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

    /**
     * Endpoint pour la prédiction de la plaque d'immatriculation à partir d'une image.
     *
     */
    @POST("v1/prediction")
    Call<PredictionResponse> predict(
            @Body PredictionRequest predictionRequest
    );

    /**
     * Test si l'API est accessible en effectuant une requête GET sur un endpoint de test.
     */
    @GET("health")
    Call<TestApiResponse> testApi(

    );

}
