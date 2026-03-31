package com.example.snaptaplaque.network.apicall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.MainActivity;
import com.example.snaptaplaque.models.api.account.ChangeProfilePictureResponse;
import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.account.DeleteAccountResponse;
import com.example.snaptaplaque.models.api.account.DeleteProfilePictureResponse;
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

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour les fonctionnalités liées au compte utilisateur.
 * * <p>Cette classe centralise toutes les opérations réseau concernant l'utilisateur :
 * authentification (login/register), gestion du profil (photo, informations personnelles),
 * export de données et suppression de compte.</p>
 * * <p>Elle utilise {@link ApiService} pour les requêtes Retrofit et {@link SessionManager}
 * pour la persistance du jeton d'authentification (JWT).</p>
 */
public class AccountCall {

    /** Instance unique du service API générée par Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    /** Gestionnaire de session pour la lecture et l'écriture du token et des préférences. */
    protected static SessionManager sessionManager;

    /**
     * Procède à l'inscription d'un nouvel utilisateur sur le serveur.
     * <p>En cas de succès, une tentative de connexion automatique (login) est immédiatement
     * lancée pour fluidifier l'expérience utilisateur.</p>
     *
     * @param activity        L'activité hôte, utilisée pour l'affichage des messages de retour.
     * @param registerRequest Objet contenant les identifiants et informations du nouvel utilisateur.
     */
    public static void register(Activity activity, RegisterRequest registerRequest) {
        apiService.register(registerRequest)
                .enqueue(new Callback<RegisterResponse>() {
                    /**
                     * Invoquée lorsque le serveur renvoie le résultat de la création de compte.
                     * @param call     L'instance de l'appel d'inscription.
                     * @param response La réponse HTTP (contenant la confirmation d'inscription).
                     */
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

                    /**
                     * Invoquée en cas de problème de communication avec le serveur lors de l'inscription.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception rencontrée (ex: erreur DNS ou serveur éteint).
                     */
                    @Override
                    public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                        FeedbackManager.showError(activity, R.string.service_unavailable + t.getMessage(), null);
                    }
                });
    }

    /**
     * Authentifie l'utilisateur et initialise la session.
     * <p>Si l'authentification réussit, le token JWT est sauvegardé via le {@link SessionManager}
     * et l'utilisateur est redirigé vers la {@link MainActivity}.</p>
     *
     * @param activity       L'activité de connexion.
     * @param request        Objet contenant le couple username/password.
     * @param sessionManager Instance du gestionnaire de session à mettre à jour.
     */
    public static void login(Activity activity, LoginRequest request, SessionManager sessionManager) {
        apiService.login(request.getUsername(), request.getPassword())
                .enqueue(new Callback<LoginResponse>() {
                    /**
                     * Invoquée lorsque le serveur renvoie un résultat de tentative de connexion.
                     * @param call     L'instance de l'appel réseau.
                     * @param response La réponse contenant les données d'accès (Token JWT).
                     */
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

                    /**
                     * Invoquée en cas d'impossibilité de joindre le serveur d'authentification.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception (ex: absence de réseau, serveur injoignable).
                     */
                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        FeedbackManager.showError(activity, "Connection error", null);
                    }
                });
    }

    /**
     * Demande l'exportation des données personnelles de l'utilisateur (conformité RGPD).
     *
     * @param apiCallback Callback pour traiter la réponse de succès ou d'échec.
     */
    public static void exportData(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.data_export("Bearer " + token)
                .enqueue(new Callback<DataExportResponse>() {
                    /**
                     * Invoquée lorsque le serveur a fini de traiter la demande d'export.
                     * @param call     L'instance de l'appel Retrofit.
                     * @param response La réponse contenant l'objet {@link DataExportResponse}
                     * (ex: lien vers un fichier JSON ou CSV).
                     */
                    @Override
                    public void onResponse(Call<DataExportResponse> call, Response<DataExportResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas de rupture de connexion ou d'impossibilité de joindre le service d'export.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception rencontrée (ex: timeout lors de la génération d'un gros fichier).
                     */
                    @Override
                    public void onFailure(Call<DataExportResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Récupère les informations détaillées du profil de l'utilisateur connecté.
     *
     * @param apiCallback Callback pour renvoyer l'objet {@link MeResponse}.
     * @param context     Contexte requis pour initialiser le {@link SessionManager}.
     */
    public static void me (ApiCallback apiCallback, Context context){

        sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();

        apiService.me("Bearer " + token)
                .enqueue(new Callback<MeResponse>() {
                    /**
                     * Invoquée lorsque le serveur répond à la requête d'identification.
                     * @param call     L'objet représentant l'appel réseau effectué.
                     * @param response La réponse contenant les données du profil (nom, prénom, etc.).
                     */
                    @Override
                    public void onResponse(Call<MeResponse> call, Response<MeResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas de problème de connectivité ou d'erreur technique lors de l'appel.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception rencontrée (ex: pas de connexion internet).
                     */
                    @Override
                    public void onFailure(Call<MeResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Supprime définitivement le compte de l'utilisateur.
     *
     * @param apiCallback Callback pour confirmer la suppression.
     */
    public static void deleteAccount(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.delete_account("Bearer " + token)
                .enqueue(new Callback<DeleteAccountResponse>() {
                    /**
                     * Invoquée lorsque le serveur renvoie un statut après la tentative de suppression.
                     * @param call     L'objet de l'appel Retrofit.
                     * @param response La réponse HTTP (contenant le message de confirmation ou l'erreur).
                     */
                    @Override
                    public void onResponse(Call<DeleteAccountResponse> call, Response<DeleteAccountResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'impossibilité de joindre le serveur ou d'erreur réseau majeure.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception rencontrée (ex: erreur de connexion au serveur).
                     */
                    @Override
                    public void onFailure(Call<DeleteAccountResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Télécharge l'image binaire de la photo de profil.
     * <p>Note : Le flux est reçu sous forme de {@link ResponseBody} car il s'agit d'un média brut.</p>
     *
     * @param apiCallback Callback pour traiter le flux de l'image.
     */
    public static void profilePicture(ApiCallback apiCallback) {
        String token = sessionManager.getToken();
        apiService.profile_picture("Bearer " + token)
                .enqueue(new Callback<ResponseBody>() {
                    /**
                     * Exécutée lors de la réception des données binaires depuis le serveur.
                     * @param call     L'objet représentant l'appel réseau.
                     * @param response La réponse contenant le flux de données (stream) de l'image.
                     */
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            apiCallback.onResponseSuccess(response);
                        } else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Exécutée en cas d'interruption du téléchargement ou d'impossibilité d'atteindre le serveur.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception (ex: Timeout pendant le transfert des données binaires).
                     */
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Met à jour la photo de profil de l'utilisateur.
     *
     * @param apiCallback Callback de résultat.
     * @param filePart    Le fichier image encapsulé dans un {@link MultipartBody.Part}.
     */
    public static void changeProfilePicture(ApiCallback apiCallback, MultipartBody.Part filePart) {
        String token = sessionManager.getToken();
        apiService.changeProfilePicture("Bearer " + token, filePart)
                .enqueue(new Callback<ChangeProfilePictureResponse>() {
                    /**
                     * Exécutée lorsque le serveur renvoie une réponse à la requête de modification.
                     * @param call     L'objet représentant l'appel réseau effectué.
                     * @param response La réponse HTTP (contenant le nouveau chemin de l'image ou l'erreur).
                     */
                    @Override
                    public void onResponse(Call<ChangeProfilePictureResponse> call, Response<ChangeProfilePictureResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Exécutée en cas d'échec critique avant que le serveur ne puisse répondre.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception lancée (souvent une coupure de connexion pendant l'upload).
                     */
                    @Override
                    public void onFailure(Call<ChangeProfilePictureResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Supprime la photo de profil actuelle de l'utilisateur.
     *
     * @param apiCallback Callback de résultat.
     */
    public static void deleteProfilePicture(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.deleteProfilePicture("Bearer " + token)
                .enqueue(new Callback<DeleteProfilePictureResponse>() {
                    /**
                     * Invoquée lors de la réception d'une réponse HTTP du serveur.
                     * @param call     L'instance de l'appel ayant généré cette réponse.
                     * @param response La réponse contenant le code de statut et le corps (body).
                     */
                    @Override
                    public void onResponse(Call<DeleteProfilePictureResponse> call, Response<DeleteProfilePictureResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée lorsqu'une erreur réseau ou une exception inattendue survient
                     * lors de l'établissement de la connexion ou du traitement de la requête.
                     * @param call L'instance de l'appel interrompu.
                     * @param t    L'exception ou l'erreur rencontrée (ex: IOException, Timeout).
                     */
                    @Override
                    public void onFailure(Call<DeleteProfilePictureResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
