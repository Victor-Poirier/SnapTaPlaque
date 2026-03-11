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

import com.example.snaptaplaque.viewmodels.SharedViewModel;

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

        VehiclesCall.getInfo(requireActivity(), plate, sharedViewModel);
    }
}
