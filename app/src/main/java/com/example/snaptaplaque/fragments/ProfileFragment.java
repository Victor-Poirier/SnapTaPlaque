package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView ivProfile;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private RecyclerView recyclerView;
    private VehicleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outil qui va chercher l'image dans le téléphone
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null) {
                        // Affiche l'image choisie par l'utilisateur
                        ivProfile.setImageURI(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedIntanceState){
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfile = view.findViewById(R.id.ivProfilePicture);

        // Lance la galerie quand on clique sur la photo
        ivProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));

        adapter = new VehicleAdapter(vehicles);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
