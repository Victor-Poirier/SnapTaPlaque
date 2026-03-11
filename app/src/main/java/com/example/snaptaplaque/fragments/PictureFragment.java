package com.example.snaptaplaque.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

public class PictureFragment extends Fragment {

    private ImageView ivLicencePlate;
    private Button btnPicture;
    private TextView showPlate;
    private Button btnSearch;
    private Photo photo;
    private SharedViewModel sharedViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    public PictureFragment() { }

    /**
     * Called when the fragment is first created.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On enregistre les launchers ICI (obligatoirement dans onCreate ou avant)
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) photo.openCamera();
                    else Toast.makeText(getContext(), R.string.necessary_camera, Toast.LENGTH_SHORT).show();
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && photo.getTempImageUri() != null) {
                        ivLicencePlate.setImageURI(null);
                        ivLicencePlate.setImageURI(photo.getTempImageUri());
                        showUI();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) ivLicencePlate.setImageURI(uri);
                    showUI();
                }
        );

        // Initialisation de l'outil Photo
        photo = new Photo(requireContext(), requestPermissionLauncher, cameraLauncher, galleryLauncher);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Assure-toi d'avoir un layout nommé fragment_picture.xml avec les bons IDs
        View view = inflater.inflate(R.layout.fragment_picture, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        ivLicencePlate = view.findViewById(R.id.ivLicencePlate);
        btnPicture = view.findViewById(R.id.btnPicture);
        showPlate = view.findViewById(R.id.showPlate);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnPicture.setOnClickListener(v -> photo.showChoice());
        btnSearch.setOnClickListener(v -> {
            String plate = showPlate.getText().toString().trim();
            if (!plate.isEmpty()) {
                VehiclesCall.getInfo(requireActivity(), plate, sharedViewModel);
            } else {
                Toast.makeText(getContext(), "Aucune plaque détectée", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Affiche les éléments de l'interface utilisateur
     */
    private void showUI() {
        ivLicencePlate.setVisibility(View.VISIBLE);
        showPlate.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.VISIBLE);
    }
}