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
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.predictions.PredictionRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.PredictionsCall;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import retrofit2.Response;

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

        btnPicture.setOnClickListener(v -> {
            photo.showChoice();
            picturePredict(photo);
            getInfoVehicle(new InfoRequest((String)showPlate.getText()));
        });

        btnSearch.setOnClickListener(v -> {
            String plate = showPlate.getText().toString().trim();
            if (!plate.isEmpty()) {
                VehiclesCall.vehicleInfo(new InfoRequest(plate), new ApiCallback() {
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

                    }
                });
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

    // Endpoint : /v1/predictions/predict
    public void picturePredict(Photo photo){
        PredictionsCall.picturePredict(new PredictionRequest(photo.getTempImageUri()), new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                showPlate.setText(response.toString());
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

    private void getInfoVehicle(InfoRequest infoRequest){
        VehiclesCall.vehicleInfo(infoRequest, new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse res = (InfoResponse) response.body();
                Vehicle vehicle = res.createVehicles(false);

                sharedViewModel.addVehicle(vehicle);
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

}