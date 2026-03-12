package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import retrofit2.Response;

public class WheelFragment extends Fragment {

    private SharedViewModel sharedViewModel;

    public WheelFragment() {
        // Constructeur par défaut
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Réalise un appel à l'API pour obtenir les informations du véhicule à partir de la plaque d'immatriculation.
     * @param plate la plaque d'immatriculation à rechercher
     */
    public void getInfo(String plate) {
        if (plate == null || plate.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez saisir une plaque", Toast.LENGTH_SHORT).show();
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
