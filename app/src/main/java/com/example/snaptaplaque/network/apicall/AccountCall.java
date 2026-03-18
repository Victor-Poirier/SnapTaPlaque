package com.example.snaptaplaque.network.apicall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.MainActivity;
import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.account.DeleteAccountResponse;
import com.example.snaptaplaque.models.api.account.LoginRequest;
import com.example.snaptaplaque.models.api.account.LoginResponse;
import com.example.snaptaplaque.models.api.account.MeResponse;
import com.example.snaptaplaque.models.api.account.RegisterRequest;
import com.example.snaptaplaque.models.api.account.RegisterResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.FeedbackManager;
import com.example.snaptaplaque.utils.SessionManager;

import org.jspecify.annotations.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * La classe {@code AccountCall} est responsable de gérer les appels API liés aux fonctionnalités de compte utilisateur dans l'application SnapTaPlaque.
 * Elle encapsule les méthodes pour l'authentification, l'inscription, la récupération des informations du compte, la suppression du compte, et l'exportation des données personnelles.
 */
public class AccountCall {
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    protected static SessionManager sessionManager;

    /**
     * Effectue l'appel API d'inscription d'un nouvel utilisateur.
     *
     * @param activity     L'activité appelante (pour le contexte UI).
     * @param registerRequest Les données d'inscription.
     */
    public static void register(Activity activity, RegisterRequest registerRequest) {
        apiService.register(registerRequest)
                .enqueue(new Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            FeedbackManager.showSuccess(activity, activity.getString(R.string.registration_success));

                            LoginRequest loginRequest = new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword());

                            AccountCall.login(activity, loginRequest, sessionManager);
                        } else {
                            FeedbackManager.showError(activity, activity.getString(R.string.registration_failed) + " " + response.message(), null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                        FeedbackManager.showError(activity, R.string.service_unavailable + t.getMessage(), null);
                    }
                });
    }

    /**
     * Effectue l'appel API de connexion d'un utilisateur.
     *
     * @param activity       L'activité appelante (pour le contexte UI).
     * @param request        Les données de connexion (nom d'utilisateur et mot de passe).
     * @param sessionManager Le gestionnaire de session pour sauvegarder le token.
     */
    public static void login(Activity activity, LoginRequest request, SessionManager sessionManager) {
        apiService.login(request.getUsername(), request.getPassword())
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().getAccessToken();

                            Log.d("LOGIN", "Login successful, token: " + token);

                            new Handler().postDelayed(() -> {
                                sessionManager.logout();
                                sessionManager.saveSession(token, request.getUsername(), request.getPassword());

                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                activity.startActivity(intent);
                                activity.finish();
                            }, 500);
                        } else {
                            FeedbackManager.showError(activity, "Invalid credentials", null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        FeedbackManager.showError(activity, "Connection error", null);
                    }
                });
    }

    public static void exportData(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.data_export("Bearer " + token)
                .enqueue(new Callback<DataExportResponse>() {
                    @Override
                    public void onResponse(Call<DataExportResponse> call, Response<DataExportResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<DataExportResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
    public static void me (ApiCallback apiCallback, Context context){

        sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();

        apiService.me("Bearer " + token)
                .enqueue(new Callback<MeResponse>() {
                    @Override
                    public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<MeResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
    public static void deleteAccount(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.delete_account("Bearer " + token)
                .enqueue(new Callback<DeleteAccountResponse>() {
                    @Override
                    public void onResponse(Call<DeleteAccountResponse> call, Response<DeleteAccountResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<DeleteAccountResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

}
