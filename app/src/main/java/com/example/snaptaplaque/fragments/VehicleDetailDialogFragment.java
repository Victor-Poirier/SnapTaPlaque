package com.example.snaptaplaque.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

/**
 * DialogFragment pour afficher les détails d'un véhicule
 * Affiche une fenêtre transparente avec les détails du véhicule et un bouton pour fermer la fenêtre
 * Utilise un ViewModel partagé pour récupérer la liste des véhicules et trouver le véhicule correspondant
 */
public class VehicleDetailDialogFragment extends DialogFragment {

    /**
     * Construit une instance de la classe VehiculeDetailDialogFragment
     */
    public static VehicleDetailDialogFragment createFrag(String immatriculation) {
        VehicleDetailDialogFragment fragment = new VehicleDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString("immatriculation", immatriculation);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rend la fenêtre transparente
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_vehicle_detail, container, false);

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Récupère l'immatriculation du véhicule sur lequel on vient de cliquer
        String immatriculation = getArguments().getString("immatriculation");

        System.out.println(immatriculation);

        // Cherche dans la liste le véhicule
        viewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            for(Vehicle v : vehicles) {
                if(v.getImmatriculation().equals(immatriculation)) {
                    ((TextView) view.findViewById(R.id.tvDetail)).setText(v.getImmatriculation() + "\n" + v.getBrand() + " " + v.getModel() + " " + v.getInfo() + "\n" + v.getEnergy());
                    break;
                }
            }
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}
