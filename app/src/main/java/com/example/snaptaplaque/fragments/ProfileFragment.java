package com.example.snaptaplaque.fragments;

import android.content.Intent;
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
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;

import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.FavoritesCall;
import com.example.snaptaplaque.network.apicall.PredictionsCall;

import com.example.snaptaplaque.utils.SessionManager;

import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;

import retrofit2.Response;

/**
 * Fragment dédié à l'affichage du profil utilisateur et de ses véhicules favoris.
 *
 * <p>Ce fragment permet à l'utilisateur de :
 * <ul>
 *     <li>Modifier sa photo de profil en sélectionnant une image depuis la galerie</li>
 *     <li>Consulter la liste de ses véhicules marqués comme favoris</li>
 *     <li>Retirer un véhicule de ses favoris en cliquant sur l'icône étoile</li>
 * </ul>
 * </p>
 *
 * <p>La liste des favoris est alimentée par le {@link SharedViewModel} partagé
 * au niveau de l'activité hôte. Toute modification de l'état favori d'un véhicule
 * (depuis {@link HistoryFragment} ou ce fragment) est automatiquement reflétée
 * grâce à l'observation du {@code LiveData} exposé par le ViewModel.</p>
 *
 * @see SharedViewModel#getFavoriteList()
 * @see SharedViewModel#toggleFavorite(com.example.snaptaplaque.models.Vehicle)
 * @see HistoryFragment
 * @see VehicleAdapter
 */
public class ProfileFragment extends Fragment {

    /**
     * ImageView affichant la photo de profil de l'utilisateur.
     */
    private ImageView ivProfile;
    private ImageView ivLogout;
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

    /**
     * Adapteur gérant l'affichage des véhicules favoris dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * ViewModel partagé avec les autres fragments pour centraliser les données véhicules et profil.
     */
    private SharedViewModel sharedViewModel;

    private SessionManager sessionManager;


    /**
     * Initialise le fragment et enregistre le lanceur de sélection d'image.
     *
     * <p>Le {@link ActivityResultLauncher} est enregistré dans {@code onCreate}
     * conformément aux recommandations du cycle de vie des fragments. Lorsqu'une
     * image est sélectionnée, elle est directement appliquée à l'{@link #ivProfile}.</p>
     *
     * @param savedInstanceState l'état précédemment sauvegardé du fragment, ou {@code null}
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outil qui ouvre la permission d'utiliser la localisation
        requestLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if(isGranted) {
                        getLastLocation();
                    }
                    else {
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
                    if(success && photo.getTempImageUri() != null) {
                        ivProfile.setImageURI(null);
                        ivProfile.setImageURI(photo.getTempImageUri());
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

        // Initialisation de la classe utilitaire Photo
        photo = new Photo(requireContext(), requestCameraPermissionLauncher, cameraLauncher, galleryLauncher);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    /**
     * Gonfle la vue du fragment et configure l'ensemble des composants graphiques.
     *
     * <p>Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li>Gonfle le layout {@code fragment_profile.xml}</li>
     *     <li>Configure le clic sur la photo de profil pour ouvrir le sélecteur d'images</li>
     *     <li>Initialise le {@link RecyclerView} avec un {@link LinearLayoutManager}</li>
     *     <li>Récupère le {@link SharedViewModel} scopé à l'activité parente</li>
     *     <li>Crée le {@link VehicleAdapter} avec un callback de toggle favori</li>
     *     <li>Observe la liste des favoris pour mettre à jour l'affichage automatiquement</li>
     * </ol>
     * </p>
     *
     * @param inflater           le {@link LayoutInflater} utilisé pour gonfler la vue
     * @param container          le conteneur parent dans lequel la vue sera insérée
     * @param savedInstanceState l'état précédemment sauvegardé du fragment, ou {@code null}
     * @return la vue racine du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(getContext());

        ivProfile = view.findViewById(R.id.ivProfilePicture);
        ivProfile.setOnClickListener(v -> photo.showChoice());

        ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            sessionManager.logout();
            Toast.makeText(getContext(), R.string.logout_success, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        ivProfile = view.findViewById(R.id.ivProfilePicture);
        ivProfile.setOnClickListener(v -> photo.showChoice());

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCountry = view.findViewById(R.id.tvCountry);

        getLastLocation();

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Affiche uniquement les favoris
        adapter = new VehicleAdapter(
                new ArrayList<>(),
                vehicle -> {
                    VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                    dialog.show(getChildFragmentManager(), "detail");
                },

                vehicle -> sharedViewModel.toggleFavorite(vehicle)
        );
        recyclerView.setAdapter(adapter);

        // Se met à jour automatiquement quand les favoris changent
        sharedViewModel.getFavoriteList().observe(getViewLifecycleOwner(), favorites -> {
            adapter.updateList(favorites);
        });

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
            // Demande 1 seule adresse correspondant aux coordonnées
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
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e(this.getClass().getName(), "Erreur Call API pour données utilisisateur pour affichage");
            }
        }, this.getContext());
    }
}
