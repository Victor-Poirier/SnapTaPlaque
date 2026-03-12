package com.example.snaptaplaque.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.account.MeResponse;
import com.example.snaptaplaque.models.api.root.HealthResponse;

import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.RootCall;

import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Response;

/**
 * Activité de lancement de l'application SnapTaPlaque.
 *
 * <p>Affiche un écran de chargement pendant la vérification de la disponibilité
 * de l'API via l'endpoint {@code /health}. Selon le résultat :</p>
 * <ul>
 *     <li>API disponible et utilisateur authentifié → redirection vers {@link MainActivity}</li>
 *     <li>API disponible et utilisateur non authentifié (HTTP 401) → redirection vers {@link SignInActivity}</li>
 *     <li>API indisponible ou erreur réseau → affichage d'une boîte de dialogue
 *         proposant de réessayer ou de quitter l'application</li>
 * </ul>
 *
 * @see RootCall#health(ApiCallback)
 * @see AccountCall#me(ApiCallback, Activity)
 */
public class LaunchActivity extends AppCompatActivity {

    /** Tag utilisé pour les messages de log liés aux tests de connexion API. */
    private static final String TAG = "API_TEST";

    /** Gestionnaire de session utilisateur (token JWT, identifiants). */
    private SessionManager sessionManager;

    /**
     * Initialise le layout de l'écran de lancement et déclenche
     * le test de connexion à l'API.
     *
     * @param savedInstanceState état sauvegardé de l'activité, ou {@code null}
     *                           lors du premier lancement
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        testApiConnection();
    }

    /**
     * Vérifie la disponibilité de l'API en interrogeant l'endpoint {@code /health}
     * via {@link RootCall#health(ApiCallback)}.
     *
     * <p>En cas de succès, enchaîne sur {@link #testConnexion()} pour vérifier
     * l'état d'authentification de l'utilisateur. En cas d'échec (erreur HTTP
     * ou erreur réseau), affiche la boîte de dialogue
     * {@link #showApiUnavailableDialog()}.</p>
     */
    private void testApiConnection() {

        RootCall.health(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                Log.e(TAG, "API available: " + response.message());
                testConnexion();
            }

            @Override
            public void onResponseFailure(Response response) {
                Log.e(TAG, "API error code: " + response.message());
                showApiUnavailableDialog();
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e(TAG, "API connection failed: " + t.getMessage());
                showApiUnavailableDialog();
            }
        });
    }

    /**
     * Vérifie si l'utilisateur dispose d'une session valide en interrogeant
     * l'endpoint protégé {@code /v1/account/me} via {@link AccountCall#me(ApiCallback, Activity)}.
     *
     * <p>Comportement selon la réponse :</p>
     * <ul>
     *     <li>HTTP 200 → l'utilisateur est authentifié, redirection vers {@link MainActivity}</li>
     *     <li>HTTP 401 → le token est absent ou expiré, redirection vers {@link SignInActivity}</li>
     *     <li>Autre code HTTP ou erreur réseau → affichage de {@link #showApiUnavailableDialog()}</li>
     * </ul>
     */
    private void testConnexion(){
        AccountCall.me(new ApiCallback() {
            Intent intent;

            @Override
            public void onResponseSuccess(Response response) {
                Log.d(TAG, "User is logged in: " + response.message());
                // HTTP 200 — l'utilisateur est authentifié
                intent = new Intent(LaunchActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onResponseFailure(Response response) {
                if (response.code() == 401) {
                    Log.d(TAG, "User is not logged in: " + response.message());
                    // HTTP 401 — Unauthorized : l'utilisateur n'est pas connecté
                    intent = new Intent(LaunchActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e(TAG, "API error code: " + response.message());
                    showApiUnavailableDialog();
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e(TAG, "API connection failed: " + t.getMessage());
                showApiUnavailableDialog();
            }
        }, this);
    }

    /**
     * Affiche une boîte de dialogue modale informant l'utilisateur que l'API
     * est indisponible, avec deux options :
     * <ul>
     *     <li><b>Réessayer</b> — relance {@link #testApiConnection()}</li>
     *     <li><b>Quitter</b> — ferme l'application via {@link Activity#finishAffinity()}</li>
     * </ul>
     *
     * <p>L'affichage est posté sur le thread principal via {@link #runOnUiThread(Runnable)}
     * car cette méthode peut être appelée depuis un callback réseau exécuté
     * sur un thread d'arrière-plan.</p>
     */
    private void showApiUnavailableDialog() {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.launch_error_title)
                .setMessage(R.string.launch_error_message)
                .setCancelable(false)
                .setPositiveButton(R.string.launch_error_retry, (dialog, which) -> testApiConnection())
                .setNegativeButton(R.string.launch_error_exit, (dialog, which) -> finishAffinity())
                .show());
    }
}
