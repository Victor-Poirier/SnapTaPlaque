package com.example.snaptaplaque.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class PictureFragment extends Fragment {

    private static final String TAG = "PictureFragment";
    private static final int UPLOAD_MAX_DIMENSION = 1280;
    private static final int UPLOAD_JPEG_QUALITY = 90;

    private ImageView ivLicencePlate;
    private Button btnPicture;
    private TextView showPlate;
    private Button btnSearch;
    private Photo photo;
    private SharedViewModel sharedViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();

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
        Uri imageUri = photo.getTempImageUri();

        if (imageUri == null) {
            Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        imageExecutor.execute(() -> {
            File file = getOptimizedJpegFromUri(imageUri);

            if (file == null) {
                runOnMainThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                });
                return;
            }

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    RequestBody.create(file, MediaType.parse("image/jpeg"))
            );

            PredictionsCall.picturePredict(body, new ApiCallback() {
                @Override
                public void onResponseSuccess(Response response) {
                    runOnMainThread(() -> {
                        setLoading(false);
                        PredictionResponse predictionResponse = (PredictionResponse) response.body();

                        if ((predictionResponse != null) && (predictionResponse.getResults() != null) && (!predictionResponse.getResults().isEmpty())) {
                            PredictionDetectionResult firstResult = predictionResponse.getResults().get(0);

                            String detectedPlate = extractPlate(firstResult.getPlaque_number());

                            if (detectedPlate != null) {
                                showPlate.setText(detectedPlate);

                                if (plateComplianceVerification(showPlate.getText().toString())) {
                                    getInfoVehicle(new InfoRequest(showPlate.getText().toString()));
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponseFailure(Response response) {
                    runOnMainThread(() -> {
                        setLoading(false);
                        Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                        if (response.code() == ApiService.ERROR_TOKEN_EXPIRE) {
                            Intent intent = new Intent(requireActivity(), SignInActivity.class);
                            requireActivity().startActivity(intent);
                        }
                    });
                }

                @Override
                public void onCallFailure(Throwable t) {
                    runOnMainThread(() -> {
                        setLoading(false);
                        Toast.makeText(getContext(), "Erreur lors de l'envoie de l'image à l'API : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
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
                Toast.makeText(getContext(), R.string.existence_plate, Toast.LENGTH_SHORT).show();
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(requireActivity(), SignInActivity.class);
                    requireActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Toast.makeText(getContext(), "Erreur lors de l'envoie de la requête : "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private File getOptimizedJpegFromUri(Uri uri) {
        try {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            try (InputStream boundsInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (boundsInputStream == null) {
                    return null;
                }
                BitmapFactory.decodeStream(boundsInputStream, null, boundsOptions);
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, UPLOAD_MAX_DIMENSION, UPLOAD_MAX_DIMENSION);
            Bitmap decodedBitmap;
            try (InputStream decodeInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (decodeInputStream == null) {
                    return null;
                }
                decodedBitmap = BitmapFactory.decodeStream(decodeInputStream, null, decodeOptions);
            }

            if (decodedBitmap == null) {
                return null;
            }

            Bitmap bitmapForUpload = resizeBitmapIfNeeded(decodedBitmap, UPLOAD_MAX_DIMENSION);

            File tempFile = new File(requireContext().getCacheDir(), "upload_" + UUID.randomUUID() + ".jpg");
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                bitmapForUpload.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, outputStream);
                outputStream.flush();
            }

            if (bitmapForUpload != decodedBitmap) {
                bitmapForUpload.recycle();
            }
            decodedBitmap.recycle();

            return tempFile;
        }
        catch(IOException e) {
            Log.e(TAG, "Unable to optimize image before upload", e);
            return null;
        }
    }

    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int largestDimension = Math.max(width, height);

        if (largestDimension <= maxDimension) {
            return bitmap;
        }

        float ratio = (float) maxDimension / largestDimension;
        int resizedWidth = Math.max(1, Math.round(width * ratio));
        int resizedHeight = Math.max(1, Math.round(height * ratio));

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }

        return Math.max(1, inSampleSize);
    }

    private void setLoading(boolean isLoading) {
        if (btnSearch != null) {
            btnSearch.setEnabled(!isLoading);
        }
    }

    private void runOnMainThread(Runnable action) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }

    @Override
    public void onDestroy() {
        imageExecutor.shutdown();
        super.onDestroy();
    }

    private String extractPlate(String noisyText) {
        if (noisyText == null) return null;

        // Nettoyage : majuscules + suppression des caractères non alphanumériques
        String cleaned = noisyText.toUpperCase().replaceAll("[^A-Z0-9]", "");

        // Regex plaque française : 2 lettres + 3 chiffres + 2 lettres (ex: AB123CD)
        Pattern pattern = Pattern.compile("[A-Z]{2}\\d{3}[A-Z]{2}");
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}