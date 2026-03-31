package com.example.snaptaplaque.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Response;

/**
 * Fragment permettant la saisie d'une plaque d'immatriculation par reconnaissance vocale.
 *
 * <p>Ce fragment utilise l'intention {@link RecognizerIntent} pour capturer la voix de l'utilisateur.
 * Il applique ensuite des filtres de nettoyage et des expressions régulières pour transformer
 * la dictée vocale en un format de plaque valide (AA-123-BB).</p>
 *
 * <p>Une fois la plaque validée, les informations du véhicule sont récupérées via l'API
 * et ajoutées à l'historique global via le {@link SharedViewModel}.</p>
 *
 * @see RecognizerIntent
 * @see SharedViewModel
 * @see VehiclesCall
 */
public class VocalFragment extends Fragment {

    /** Code de requête pour identifier le retour de l'activité de reconnaissance vocale. */
    private static final int REQUEST_CODE_SPEECH = 101;
    /** Champ de saisie affichant la plaque dictée ou modifiée manuellement. */
    private TextInputEditText numberPlate;
    /** Conteneur du champ de saisie gérant l'icône de déclenchement vocal. */
    private TextInputLayout btnVocal;
    /** Bouton lançant la recherche du véhicule auprès de l'API. */
    private Button btnSearch;
    /** ViewModel partagé pour la persistance de l'historique durant la session. */
    private SharedViewModel sharedViewModel;

    /**
     * Initialise l'interface utilisateur et les écouteurs d'événements.
     *
     * @param inflater           Le {@link LayoutInflater}.
     * @param container          Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue racine {@code fragment_vocal.xml}.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vocal, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        numberPlate = view.findViewById(R.id.numberPlate);
        btnVocal = view.findViewById(R.id.btnVocal);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnVocal.setEndIconOnClickListener(v -> askSpeechInput());
        btnSearch.setOnClickListener(v -> {
            if(plateComplianceVerification(numberPlate.getText().toString())) {
                getInfoVehicle(new InfoRequest(numberPlate.getText().toString()));
            }
        });

        return view;
    }

    /**
     * Lance l'interface système de reconnaissance vocale de Google.
     *
     * <p>Configure l'intention avec un modèle de langage libre et la locale par défaut
     * de l'appareil. Affiche un message d'erreur si aucun service de reconnaissance n'est disponible.</p>
     */
    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.spell_plate);

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
        } catch(Exception e) {
            Toast.makeText(getContext(), R.string.micro_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Récupère le résultat de la dictée vocale et formate la plaque.
     *
     * <p>Le traitement inclut :
     * 1. L'extraction de la meilleure correspondance textuelle.
     * 2. Le nettoyage des caractères non alphanumériques.
     * 3. Le formatage automatique avec tirets pour les plaques de 7 caractères (SIV).</p>
     *
     * @param requestCode Le code de la requête (doit être {@link #REQUEST_CODE_SPEECH}).
     * @param resultCode  Le code de résultat de l'activité.
     * @param data        L'intention contenant les résultats textuels.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == REQUEST_CODE_SPEECH) && (resultCode == Activity.RESULT_OK) && (data != null)) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if((result != null) && (!result.isEmpty())) {
                String plate = result.get(0).toUpperCase().replaceAll("[^A-Z0-9]", "");

                if(plate.length() > 7) {
                    plate = plate.substring(0, 7);
                }

                if(plate.length() == 7) {
                    plate = plate.replaceFirst("((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})", "$1-$2-$3");
                }

                numberPlate.setText(plate);
            }
        }
    }

    /**
     * Vérifie la conformité de la plaque par rapport aux standards français (SIV).
     *
     * @param plate La chaîne de caractères à vérifier.
     * @return {@code true} si la plaque respecte le format avec ou sans tirets, {@code false} sinon.
     */
    private boolean plateComplianceVerification(String plate) {

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS|WW)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})";

        if((!plate.matches(regex_1)) && (!plate.matches(regex_2))) {
            Toast.makeText(getContext(), R.string.compliance_plate, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Appelle le service API pour récupérer les informations du véhicule.
     *
     * <p>En cas de succès :
     * <ul>
     * <li>Le véhicule est créé et ajouté au {@link SharedViewModel}.</li>
     * <li>Le {@link VehicleDetailDialogFragment} est affiché pour présenter les détails.</li>
     * </ul>
     * </p>
     *
     * @param infoRequest Objet contenant l'immatriculation à rechercher.
     */
    private void getInfoVehicle(InfoRequest infoRequest){
        VehiclesCall.vehicleInfo(infoRequest, new ApiCallback() {
            /**
             * Traite les données du véhicule reçues et ouvre le dialogue de détails.
             *
             * @param response Réponse contenant l'objet {@link InfoResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse res = (InfoResponse) response.body();
                Vehicle vehicle = res.createVehicles(false);

                sharedViewModel.addVehicle(vehicle);

                VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                dialog.show(getChildFragmentManager(), "detail");
            }

            /**
             * Gère les erreurs serveur ou les plaques inexistantes.
             *
             * @param response Réponse d'erreur.
             */
            @Override
            public void onResponseFailure(Response response) {
                Toast.makeText(getContext(), R.string.existence_plate, Toast.LENGTH_SHORT).show();
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            /**
             * Gère l'échec de la communication réseau.
             *
             * @param t L'exception rencontrée.
             */
            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }
}
