package com.example.snaptaplaque.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Photo;

public class PictureFragment extends Fragment {

    private ImageView ivLicencePlate;
    private Button btnSearch;
    private Photo photo;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    public PictureFragment() { }

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
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) ivLicencePlate.setImageURI(uri);
                }
        );

        // Initialisation de l'outil Photo
        photo = new Photo(requireContext(), requestPermissionLauncher, cameraLauncher, galleryLauncher);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Assure-toi d'avoir un layout nommé fragment_picture.xml avec les bons IDs
        View view = inflater.inflate(R.layout.fragment_picture, container, false);

        ivLicencePlate = view.findViewById(R.id.ivLicencePlate);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> photo.showChoice());

        return view;
    }
}