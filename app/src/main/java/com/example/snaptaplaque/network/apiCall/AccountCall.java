package com.example.snaptaplaque.network.apiCall;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.snaptaplaque.activities.MainActivity;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.api.account.LoginRequest;
import com.example.snaptaplaque.models.api.account.LoginResponse;
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
                            FeedbackManager.showSuccess(activity, "Registration successful");
                            Intent intent = new Intent(activity, SignInActivity.class);
                            activity.startActivity(intent);
                            activity.finish();
                        } else {
                            FeedbackManager.showError(activity, "Registration failed: " + response.message(), null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                        FeedbackManager.showError(activity, "Network error: " + t.getMessage(), null);
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
        apiService.login(request)
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String token = response.body().getAccessToken();

                            Log.d("token", token);

                            new Handler().postDelayed(() -> {
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

    public static void deleteAccount(){

    }

    public static void me(){

    }

    public static void exportData(){

    }

}