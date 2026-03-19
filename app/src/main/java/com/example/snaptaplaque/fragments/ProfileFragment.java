package com.example.snaptaplaque.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.models.api.account.MeResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.utils.SessionManager;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Fragment responsable de l'affichage et de la gestion du profil utilisateur.
 */
public class ProfileFragment extends Fragment {

    private ImageView ivProfile;
    private ImageView ivLogout;
    private ImageView ivApiInfo;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvCountry;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private Photo photo;
    private RecyclerView recyclerView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private VehicleAdapter adapter;
    private SharedViewModel sharedViewModel;
    private SessionManager sessionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outil qui ouvre la permission d'utiliser la localisation
        requestLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if(isGranted) {
                        getLastLocation();
                    }else {
                        Toast.makeText(getContext(), R.string.necessary_gps, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Outil qui ouvre la permission d'utiliser la caméra
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if(isGranted) {
                        photo.openCamera();
                    }else {
                        Toast.makeText(getContext(), R.string.necessary_camera, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Outil qui ouvre l'appareil photo
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if(success && photo.getTempImageUri() != null) {
                        ivProfile.setImageURI(null);
                        ivProfile.setImageURI(photo.getTempImageUri());
                        // APPEL API : Envoyer la photo prise par la caméra
                        changeProfilePicture(photo.getTempImageUri());
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
                        // APPEL API : Envoyer la photo choisie dans la galerie
                        changeProfilePicture(uri);
                    }
                }
        );

        // Initialisation de la classe utilitaire Photo
        photo = new Photo(requireContext(), requestCameraPermissionLauncher, cameraLauncher, galleryLauncher);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(getContext());

        ivProfile = view.findViewById(R.id.ivProfilePicture);
        ivProfile.setOnClickListener(v -> {
            photo.showChoice();
        });

        ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            sessionManager.logout();
            Toast.makeText(getContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        ivApiInfo = view.findViewById(R.id.ivComplementaryInfo);
        ivApiInfo.setOnClickListener(v -> {
            ProfileAdditionalInformationFragment frag = ProfileAdditionalInformationFragment.createFrag();
            frag.show(getChildFragmentManager(), "Extension");
        });

        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText("Default Username");
        tvEmail = view.findViewById(R.id.tvEmail);
        tvEmail.setText("Default email@example.com");
        tvCountry = view.findViewById(R.id.tvCountry);


        getUserInfo();
        getLastLocation();

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        adapter = new VehicleAdapter(
                new ArrayList<>(),
                vehicle -> {
                    VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                    dialog.show(getChildFragmentManager(), "detail");
                },

                vehicle -> sharedViewModel.toggleFavorite(vehicle),
                this.getActivity()
        );
        recyclerView.setAdapter(adapter);

        sharedViewModel.getFavoriteList().observe(getViewLifecycleOwner(), favorites -> {
            adapter.updateList(favorites);
        });

        sharedViewModel.loadDataIfNeeded();

        return view;
    }

    private void getLastLocation() {
        if(androidx.core.app.ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);

            tvCountry.setText(R.string.country);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if(location != null) {
                        tvCountry.setText(getCityName(location.getLatitude(), location.getLongitude()));
                    }
                    else {
                        tvCountry.setText(R.string.country);
                    }
                });
    }

    private String getCityName(double latitude, double longitude) {
        String Location = getString(R.string.country);
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if((addresses != null) && (!addresses.isEmpty())) {
                Address address = addresses.get(0);

                String cityName = address.getLocality();
                String subName = address.getSubLocality();
                String adminAreaName = address.getAdminArea();
                String countryName = address.getCountryName();

                if(((cityName != null) && (!cityName.isEmpty())) &&
                        ((adminAreaName != null) && (!adminAreaName.isEmpty()))) {

                    Location = cityName + ", " + adminAreaName;
                }
                else if(((subName != null) && (!subName.isEmpty())) &&
                        ((adminAreaName != null) && (!adminAreaName.isEmpty()))) {

                    Location = subName + ", " + adminAreaName;
                }
                else if((adminAreaName != null) && (!adminAreaName.isEmpty()) &&
                        ((countryName != null) && (!countryName.isEmpty()))) {
                    Location = adminAreaName + ", " + countryName;
                }
                else {
                    Location = countryName;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Location = getString(R.string.country);
        }

        return Location;
    }

    public void getUserInfo(){
        AccountCall.me(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                MeResponse res = (MeResponse)response.body();
                tvUsername.setText(res.getUsername());
                tvEmail.setText(res.getEmail());
            }

            @Override
            public void onResponseFailure(Response response) {
                Log.e(this.getClass().getName(), "Erreur récupération données utilisisateur pour affichage");
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e(this.getClass().getName(), "Erreur Call API pour données utilisisateur pour affichage");
            }
        }, this.getContext());

        AccountCall.profilePicture(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                ResponseBody body = (ResponseBody) response.body();
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                ivProfile.setImageBitmap(bitmap);
            }

            @Override
            public void onResponseFailure(Response response) {
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
            }
        });

    }

    /**
     * Convertit l'Uri en File et compresse l'image en JPEG pour réduire sa taille.
     */
    private java.io.File prepareImageFile(Uri uri) {
        try {
            // Créer un fichier temporaire dans le cache
            java.io.File tempFile = new java.io.File(requireContext().getCacheDir(), "upload_profile.jpg");

            // Lire l'image depuis l'URI
            java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if(bitmap == null) return null;

            // Écrire le bitmap compressé dans le fichier temporaire
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            // Compression JPEG qualité 80%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            return tempFile;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void changeProfilePicture(Uri uri) {
        if (uri == null) {
            Toast.makeText(getContext(), "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            return;
        }

        java.io.File file = prepareImageFile(uri);
        if (file == null) {
            Toast.makeText(getContext(), "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show();
            return;
        }

        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("image/jpeg"),
                file
        );

        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        AccountCall.changeProfilePicture(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
            }

            @Override
            public void onResponseFailure(Response response) {
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
            }
        }, body);
    }

}
