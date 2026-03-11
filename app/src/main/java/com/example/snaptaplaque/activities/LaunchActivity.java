package com.example.snaptaplaque.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.root.TestApiResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activité de lancement qui affiche un écran de chargement et teste la connexion à l'API.
 * Si la connexion est réussie, redirige vers MainActivity ou LoginActivity selon la session.
 * Si la connexion échoue, affiche une boîte de dialogue pour réessayer ou quitter.
 */
public class LaunchActivity extends AppCompatActivity {

    private static final String TAG = "API_TEST";

    /** Durée de 5 secondes pour le timeout de la connexion à l'API */
    private static final int API_TIMEOUT_MS = 5000; // 5 secondes

    /** Instance de l'interface ApiService pour effectuer les appels API */
    private ApiService apiService;

    /** Instance de SessionManager pour gérer la session utilisateur et les données de connexion */
    private SessionManager sessionManager;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialise les composants nécessaires et teste la connexion à l'API.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Initialiser ApiService
        apiService = ApiClient.getRetrofit().create(ApiService.class);

        // On essaie de se connecter à l'API en utilisant l'endpoint protégé /v1/account/me
        // Si l'api renvoie le code 401 - Unauthorized alors, on doit se connecter pour utiliser l'application.
        // Si l'api renvoie le code 200 - OK alors, on peut accéder à l'application directement.


        // Initialiser SessionManager
        sessionManager = new SessionManager(getApplicationContext());

        // Debug : vérifier l'état de la session au lancement
        Log.d(TAG, "isLoggedIn: " + sessionManager.isLoggedIn());
        Log.d(TAG, "token: " + sessionManager.getToken());
        Log.d(TAG, "username: " + sessionManager.getUsername());

        testApiConnection();
    }

    /**
     * Méthode qui teste la connexion à l'API en effectuant une requête de connexion avec des identifiants de test.
     * Si la connexion est réussie, affiche un message de succès et redirige vers l'activité suivante.
     * Si la connexion échoue, affiche un message d'erreur et propose à l'utilisateur de réessayer ou de quitter.
     */
    private void testApiConnection() {
        Call<TestApiResponse> call = apiService.health();

        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            call.cancel();
            Log.e(TAG, "API timeout after " + API_TIMEOUT_MS + "ms");
            showApiUnavailableDialog();
        };
        timeoutHandler.postDelayed(timeoutRunnable, API_TIMEOUT_MS);

        call.enqueue(new Callback<TestApiResponse>() {
            @Override
            public void onResponse(Call<TestApiResponse> c, Response<TestApiResponse> response) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "API available — status: " + response.body().getStatus());
                    proceedToApp();
                } else {
                    Log.e(TAG, "API error code: " + response.code());
                    showApiUnavailableDialog();
                }
            }

            @Override
            public void onFailure(Call<TestApiResponse> c, Throwable t) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (!c.isCanceled()) {
                    Log.e(TAG, "API connection failed: " + t.getMessage());
                    showApiUnavailableDialog();
                }
            }
        });
    }

    private void proceedToApp() {
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showApiUnavailableDialog() {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle("Service indisponible")
                .setMessage("Impossible de se connecter au serveur. Vérifiez votre connexion et réessayez.")
                .setCancelable(false)
                .setPositiveButton("Réessayer", (dialog, which) -> testApiConnection())
                .setNegativeButton("Quitter", (dialog, which) -> finishAffinity())
                .show());
    }
}
