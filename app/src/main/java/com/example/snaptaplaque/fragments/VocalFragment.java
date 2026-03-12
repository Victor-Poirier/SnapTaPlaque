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
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Response;

public class VocalFragment extends Fragment {

    private static final int REQUEST_CODE_SPEECH = 101;
    private TextInputEditText numberPlate;
    private TextInputLayout btnVocal;
    private Button btnSearch;
    private SharedViewModel sharedViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_vocal, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        numberPlate = view.findViewById(R.id.numberPlate);
        btnVocal = view.findViewById(R.id.btnVocal);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnVocal.setEndIconOnClickListener(v -> askSpeechInput());
        btnSearch.setOnClickListener(v -> getInfo(numberPlate.getText().toString().trim()));

        return view;
    }

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

    private void showToast() {
        String plate = numberPlate.getText().toString().trim();

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS|WW)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})";

        if(plate.matches(regex_1) || plate.matches(regex_2)) {
            Toast.makeText(getContext(), R.string.validate_plate + " ✅", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), R.string.unvalidate_plate + " ❌", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Réalise un appel à l'API pour obtenir les informations du véhicule à partir de la plaque d'immatriculation.
     * @param plate la plaque d'immatriculation à rechercher
     */
    private void getInfo(String plate) {
        if (plate == null || plate.isEmpty()) {
            Toast.makeText(getContext(), R.string.hint_immatriculation, Toast.LENGTH_SHORT).show();
            return;
        }

        VehiclesCall.getInfo(plate, sharedViewModel, new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse info = (InfoResponse) response.body();
                if (info != null) {
                    Vehicle vehicle = new Vehicle(
                            info.getLicensePlate(),
                            info.getBrand(),
                            info.getModel(),
                            info.getInfo(),
                            info.getEnergy(),
                            false
                    );
                    sharedViewModel.addVehicle(vehicle);
                }
            }

            @Override
            public void onResponseFailure(Response response) {
                Toast.makeText(getContext(), "Véhicule non trouvé", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCallFailure(Throwable t) {
                Toast.makeText(getContext(), "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
