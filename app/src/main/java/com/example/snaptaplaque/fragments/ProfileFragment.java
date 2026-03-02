package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

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
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri tempImageUri;
    private RecyclerView recyclerView;
    private VehicleAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outil qui ouvre la permission d'utiliser la caméra
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if(isGranted) {
                        openCamera();
                    }
                    else {
                        Toast.makeText(getContext(), R.string.necessary_camera, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Outil qui ouvre l'appareil photo
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if(success && tempImageUri != null) {
                        ivProfile.setImageURI(tempImageUri);
                    }
                }
        );

        // Outil qui ouvre la galerie du téléphone
        galleryLauncher = registerForActivityResult(
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

        ivProfile.setOnClickListener(v -> showChoice());

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

    private void showChoice() {
        String[] options = {
                getString(R.string.camera_choice),
                getString(R.string.gallery_choice)
        };

        // Récupère le choix de l'utlisateur
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_photo)
                .setItems(options, (dialog, which) -> {
                    if(which == 0) {
                        checkCameraPermission();
                    }
                    else {
                        galleryLauncher.launch("image/*");
                    }
                }).show();
    }

    private void checkCameraPermission() {
        // Vérifie la permission donnée par l'utilisateur pour l'utilisaton de la caméra
        if(androidx.core.content.ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {

            openCamera();
        }
        else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        // Crée un fichier vide pour recevoir la photo
        java.io.File tempFile = new java.io.File(requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "temp_image.jpg");

        // Transforme le fichier en URI (image)
        tempImageUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                tempFile
        );

        cameraLauncher.launch(tempImageUri);
    }
}
