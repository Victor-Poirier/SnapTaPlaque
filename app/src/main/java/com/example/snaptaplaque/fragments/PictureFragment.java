package com.example.snaptaplaque.fragments;

import android.content.Intent;
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
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.predictions.PredictionDetectionResult;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.PredictionsCall;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.HTTP;

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
                    if(success && photo.getTempImageUri() != null) {
                        ivLicencePlate.setImageURI(null);
                        ivLicencePlate.setImageURI(photo.getTempImageUri());
                        showUI();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null) {
                        photo.setTempImageUri(uri);
                        ivLicencePlate.setImageURI(null);
                        ivLicencePlate.setImageURI(photo.getTempImageUri());
                        showUI();
                    }
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
        View view = inflater.inflate(R.layout.fragment_picture, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        ivLicencePlate = view.findViewById(R.id.ivLicencePlate);
        btnPicture = view.findViewById(R.id.btnPicture);
        showPlate = view.findViewById(R.id.showPlate);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnPicture.setOnClickListener(v -> {
            photo.showChoice();
        });

        btnSearch.setOnClickListener(v -> {
            picturePredict(photo);
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

    public void picturePredict(Photo photo){
        File file = getFileFromUri(photo.getTempImageUri());

        if(file == null) {
            return;
        }

        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "file",
                file.getName(),
                RequestBody.create(MediaType.parse("image/jpeg"), file)
        );

        PredictionsCall.picturePredict(body, new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                PredictionResponse predictionResponse = (PredictionResponse) response.body();

                if((predictionResponse != null) && (predictionResponse.getResults() != null) && (!predictionResponse.getResults().isEmpty())) {
                    PredictionDetectionResult firstResult = predictionResponse.getResults().get(0);

                    String detectedPlate = firstResult.getPlaque_number();

                    showPlate.setText(detectedPlate);

                    if(plateComplianceVerification(showPlate.getText().toString())) {
                        getInfoVehicle(new InfoRequest(showPlate.getText().toString()));
                    }
                } else {
                    Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponseFailure(Response response) {
                Integer errorCode = response.code();
                Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Toast.makeText(getContext(), "Erreur lors de l'envoie de l'image à l'API : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean plateComplianceVerification(String plate) {

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS|WW)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})";

        if((!plate.matches(regex_1)) && (!plate.matches(regex_2))) {
            Toast.makeText(getContext(), R.string.compliance_plate, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void getInfoVehicle(InfoRequest infoRequest){
        VehiclesCall.vehicleInfo(infoRequest, new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse res = (InfoResponse) response.body();
                Vehicle vehicle = res.createVehicles(false);

                sharedViewModel.addVehicle(vehicle);

                VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                dialog.show(getChildFragmentManager(), "detail");
            }

                @Override
                public void onResponseFailure(Response response) {
                    Integer errorCode = response.code();
                    Toast.makeText(getContext(), R.string.existence_plate, Toast.LENGTH_SHORT).show();
                    if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                        Intent intent = new Intent(getActivity(), SignInActivity.class);
                        getActivity().startActivity(intent);
                    }
                }

                @Override
                public void onCallFailure(Throwable t) {
                    Toast.makeText(getContext(), "Erreur lors de l'envoie de la requête : "+ t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private File getFileFromUri(Uri uri) {
        try {
            File tempFile = new File(requireContext().getCacheDir(), "image.jpg");

            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[8192];
            int read;

            while((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return tempFile;
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}