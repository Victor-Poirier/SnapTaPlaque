package com.example.snaptaplaque.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.activities.SignUpActivity;
import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.ModelCall;
import com.example.snaptaplaque.network.apicall.RootCall;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Response;

/**
 * Fragment de dialogue affichant les informations techniques et les options de gestion de compte.
 *
 * <p>Ce fragment plein écran permet à l'utilisateur de :
 * <ul>
 * <li>Consulter les versions de l'API et les informations du modèle de ML</li>
 * <li>Exporter ses données personnelles au format JSON sur le stockage local</li>
 * <li>Supprimer définitivement son compte utilisateur</li>
 * </ul>
 * </p>
 *
 * <p>Toutes les actions sensibles (export, suppression) sont synchronisées avec le
 * {@link SessionManager} pour garantir la sécurité des accès.</p>
 *
 * @see DialogFragment
 * @see AccountCall
 * @see SessionManager
 */
public class ProfileAdditionalInformationFragment extends DialogFragment {
    private ApiService apiService;
    private Button bn_export_data;
    private TextView tv_export_data_response;
    private TextView tv_api_version;
    private TextView tv_model_info;
    private Button bn_delete_account;
    private Button bn_close;

    /** Gère l'état de la session utilisateur pour la déconnexion après suppression. */
    SessionManager sessionManager;

    /**
     * Méthode statique de création du fragment.
     *
     * @return Une instance configurée de {@link ProfileAdditionalInformationFragment}.
     */
    public static ProfileAdditionalInformationFragment createFrag() {
        ProfileAdditionalInformationFragment fragment = new ProfileAdditionalInformationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Configure le style du dialogue en mode plein écran sans bordures.
     *
     * @param savedInstanceState État sauvegardé.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    /**
     * Initialise l'interface utilisateur, les services API et les écouteurs d'événements.
     *
     * @param inflater Le gonfleur de vue.
     * @param container Le conteneur parent.
     * @param savedInstanceState État sauvegardé.
     * @return La vue racine du fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());

        apiService = ApiClient.getRetrofit().create(ApiService.class);
        View view = inflater.inflate(R.layout.fragment_info_api, container, false);

        bn_export_data = view.findViewById(R.id.btnDataExport);
        tv_export_data_response = view.findViewById(R.id.tvDataExportResponse);
        tv_api_version = view.findViewById(R.id.tvApiVersionResponse);
        tv_model_info = view.findViewById(R.id.tvModelInfoResponse);
        bn_delete_account = view.findViewById(R.id.btnDeleteAccount);
        bn_close = view.findViewById(R.id.btnClose);

        // Listener pour l'export des données RGPD
        bn_export_data.setOnClickListener(v->{
            exportUserData();
        });

        // Listener pour la suppression du compte et redirection
        bn_delete_account.setOnClickListener(v->{
            deleteAccount();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            sessionManager.logout();
            Toast.makeText(getContext(), R.string.delete_account_successful, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        bn_close.setOnClickListener(v -> dismiss());

        // Chargement initial des infos techniques
        modelInfo();
        apiVersion();

        return view;
    }

    /**
     * Récupère les données utilisateur via l'API et les sauvegarde dans un fichier JSON local.
     *
     * <p>Le fichier est enregistré dans le répertoire {@code Documents} privé de l'application
     * (ExternalFilesDir) pour respecter les politiques d'accès au stockage d'Android.</p>
     */
    public void exportUserData() {
        AccountCall.exportData(new ApiCallback() {
            /**
             * Traite la réponse positive de l'API et orchestre l'écriture du fichier sur le disque.
             *
             * <p>Le processus se déroule comme suit :
             * <ol>
             * <li>Conversion de l'objet {@link DataExportResponse} en chaîne JSON via GSON.</li>
             * <li>Détermination du répertoire de stockage sécurisé.</li>
             * <li>Écriture physique du fichier {@code user_data_export.json}.</li>
             * <li>Mise à jour du {@link TextView} pour afficher le chemin absolu du fichier généré.</li>
             * </ol>
             * </p>
             *
             * @param response Réponse contenant l'objet de données {@link DataExportResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                DataExportResponse res = (DataExportResponse) response.body();

                if (res != null && getContext() != null) {
                    try {
                        String jsonString = new com.google.gson.Gson().toJson(res);

                        // Définit le chemin : Android/data/com.package.../files/Documents/
                        java.io.File directory = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                        if (directory == null) {
                            // Fallback if external storage is unavailable
                            directory = requireContext().getFilesDir();
                        }

                        java.io.File file = new java.io.File(directory, "user_data_export.json");

                        java.io.FileWriter writer = new java.io.FileWriter(file);
                        writer.write(jsonString);
                        writer.flush();
                        writer.close();

                        String successMessage = "File saved at:\n" + file.getAbsolutePath();
                        tv_export_data_response.setText(successMessage);

                    } catch (java.io.IOException e) {
                        tv_export_data_response.setText("Error: Saving data isn't possible currently");
                    }
                } else {
                    tv_export_data_response.setText("Error: No data available to export.");
                }
            }

            /**
             * Gère les erreurs renvoyées par l'API lors de la demande d'export.
             * * <p>En cas de jeton expiré ({@link ApiService#ERROR_TOKEN_EXPIRE}), l'utilisateur
             * est redirigé vers {@link SignInActivity}.</p>
             *
             * @param response La réponse d'erreur HTTP.
             */
            @Override
            public void onResponseFailure(Response response) {
                if (response.code() == ApiService.ERROR_TOKEN_EXPIRE) {
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    if (getActivity() != null) {
                        getActivity().startActivity(intent);
                    }
                } else {
                    tv_export_data_response.setText("API Error: " + response.code());
                }
            }

            /**
             * Gère les échecs de connexion réseau ou les erreurs de timeout.
             *
             * @param t L'exception {@link Throwable} rencontrée.
             */
            @Override
            public void onCallFailure(Throwable t) {
                Log.e("ProfileInfo", t.getMessage());
            }
        });
    }

    /**
     * Déclenche la suppression définitive du compte utilisateur sur le serveur distant.
     *
     * <p>Cette méthode effectue un appel asynchrone sécurisé via {@link AccountCall#deleteAccount}.
     * En cas de succès, toutes les données liées à l'utilisateur sont supprimées de la base
     * de données distante conformément aux directives RGPD.</p>
     */
    public void deleteAccount(){
        AccountCall.deleteAccount(new ApiCallback() {
            /**
             * Traite la confirmation de suppression envoyée par le serveur.
             *
             * <p>Une fois la suppression validée, l'utilisateur est redirigé vers
             * l'écran de création de compte ({@link SignUpActivity}).</p>
             *
             * @param response Objet {@link Response} confirmant le succès de l'opération.
             */
            @Override
            public void onResponseSuccess(Response response) {
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                getActivity().startActivity(intent);
            }

            /**
             * Gère les erreurs de réponse du serveur lors de la tentative de suppression.
             *
             * <p>Si le jeton d'authentification a expiré ({@link ApiService#ERROR_TOKEN_EXPIRE}),
             * l'utilisateur est renvoyé vers l'écran de connexion ({@link SignInActivity}).</p>
             *
             * @param response La réponse d'erreur contenant le code HTTP.
             */
            @Override
            public void onResponseFailure(Response response) {
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            /**
             * Gère les échecs de communication réseau ou les interruptions inattendues.
             *
             * @param t L'exception {@link Throwable} décrivant l'erreur de connexion.
             */
            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

    /**
     * Récupère la version actuelle de l'API distante.
     *
     * <p>Cette méthode interroge le point de terminaison racine de l'API via {@link RootCall#apiVersion}
     * pour s'assurer de la compatibilité entre le client mobile et le serveur backend.</p>
     */
    public void apiVersion(){
        RootCall.apiVersion(new ApiCallback() {
            /**
             * Traite la réponse positive de l'API contenant les informations de version.
             *
             * <p>En cas de succès, l'objet {@link ApiVersionResponse} est extrait pour
             * mettre à jour l'affichage de la version dans le {@link TextView} dédié.</p>
             *
             * @param response Objet {@link Response} transportant les données de version.
             */
            @Override
            public void onResponseSuccess(Response response) {

            }

            /**
             * Gère les cas où le serveur retourne un code d'erreur (ex: 404, 503).
             *
             * @param response La réponse d'erreur brute reçue du serveur.
             */
            @Override
            public void onResponseFailure(Response response) {

            }

            /**
             * Gère les échecs critiques de communication (absence de réseau, timeout).
             *
             * @param t L'exception {@link Throwable} détaillant la cause de l'interruption.
             */
            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

    /**
     * Récupère et affiche les métadonnées du modèle de reconnaissance utilisé par l'application.
     * * <p>Cette méthode effectue un appel asynchrone via {@link ModelCall#modelInfo} pour obtenir
     * les détails techniques du modèle ML (version, date d'entraînement, précision, etc.).</p>
     */
    public void modelInfo(){
        ModelCall.modelInfo(new ApiCallback() {
            /**
             * Traite la réponse positive de l'API et met à jour l'interface utilisateur.
             *
             * <p>Le contenu est formaté en une chaîne de caractères lisible via la méthode
             * {@link ModelInfoResponse#createString(android.content.Context)} avant d'être
             * injecté dans le TextView dédié.</p>
             *
             * @param response Objet {@link Response} contenant le corps {@link ModelInfoResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                ModelInfoResponse res = (ModelInfoResponse) response.body();
                tv_model_info.setText(res.createString(getContext()));
            }

            /**
             * Gère les erreurs renvoyées par le serveur (ex: 404, 500).
             *
             * @param response La réponse d'erreur reçue de l'API.
             */
            @Override
            public void onResponseFailure(Response response) {

            }

            /**
             * Gère les échecs de communication réseau ou les exceptions lors de l'appel.
             *
             * @param t L'exception {@link Throwable} décrivant la cause de l'échec.
             */
            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }
}