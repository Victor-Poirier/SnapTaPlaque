package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.RootCall;
import retrofit2.Response;

/**
 * Fragment de dialogue plein écran affichant la politique de confidentialité (RGPD) de l'application.
 *
 * <p>Ce fragment récupère dynamiquement le contenu textuel depuis l'API via {@link RootCall#privacyPolicy},
 * en adaptant la langue du contenu selon les paramètres régionaux (Locale) de l'appareil de l'utilisateur.</p>
 *
 * <p>L'affichage est configuré en mode plein écran sans barre de titre pour maximiser l'espace
 * de lecture des informations juridiques.</p>
 *
 * @see DialogFragment
 * @see RootCall
 * @see com.example.snaptaplaque.models.api.root.RgpdResponse
 */
public class PrivacyPolicyDialogFragment extends DialogFragment {

    /**
     * Méthode statique de création pour instancier le fragment.
     *
     * @return Une nouvelle instance de {@link PrivacyPolicyDialogFragment}.
     */
     public static PrivacyPolicyDialogFragment createFrag() {
        return new PrivacyPolicyDialogFragment();
    }

    /**
     * Constructeur par défaut.
     * <p>Requis par le système Android pour la reconstruction du fragment lors des changements de configuration.</p>
     */
    public PrivacyPolicyDialogFragment() {
        // Constructeur vide requis pour les DialogFragments
    }

    /**
     * Définit le style du dialogue lors de sa création.
     * <p>Configure le fragment pour s'afficher en plein écran avec un thème sombre sans barre de titre.</p>
     *
     * @param savedInstanceState État sauvegardé du fragment (non utilisé).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    /**
     * Initialise la vue du dialogue, gère la transparence et déclenche l'appel réseau pour le contenu.
     *
     * <p>Le contenu est chargé de manière asynchrone. Une fois la réponse reçue, le texte est formaté
     * via {@link RgpdResponse#createString} puis injecté dans le {@link TextView} sur le thread UI.</p>
     *
     * @param inflater           Le {@link LayoutInflater} pour gonfler la vue.
     * @param container          Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue {@link View} racine du dialogue.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rend la fenêtre transparente
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        View view = inflater.inflate(R.layout.privacy_dialog, container, false);

        RootCall.privacyPolicy(new ApiCallback() {
            /**
             * Succès de la requête — Formate et affiche le texte RGPD.
             *
             * @param response La réponse HTTP contenant l'objet {@link RgpdResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String privacyText = ((RgpdResponse) response.body()).createString(getContext());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                TextView privacyPolicyRV = view.findViewById(R.id.tvPrivacyContent);
                                privacyPolicyRV.setText(privacyText);
                            });
                        }
                    }
            }

            /**
             * Échec de la réponse HTTP.
             *
             * @param response La réponse en erreur retournée par l'API.
             */
            @Override
            public void onResponseFailure(Response response) {

            }

            /**
             * Erreur lors de l'appel réseau (timeout, pas d'internet, etc.).
             *
             * @param t L'exception levée.
             */
            @Override
            public void onCallFailure(Throwable t) {

            }
        }, getResources().getConfiguration().locale.getLanguage());

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}
