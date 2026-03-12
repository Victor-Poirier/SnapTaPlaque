package com.example.snaptaplaque.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.snaptaplaque.utils.SessionManager;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/**
 * Fragment responsable de l'affichage et de la gestion du profil utilisateur.
 *
 * <p>Ce fragment présente les informations personnelles de l'utilisateur et affiche
 * la liste de ses véhicules favoris. Il fournit également des fonctionnalités pour :
 * <ul>
 *     <li>Modifier la photo de profil (appareil photo ou galerie)</li>
 *     <li>Afficher la localisation actuelle de l'utilisateur</li>
 *     <li>Gérer les véhicules favoris via une {@link RecyclerView}</li>
 *     <li>Se déconnecter de l'application</li>
 * </ul>
 * </p>
 *
 * <p>Le fragment utilise le {@link com.example.snaptaplaque.viewmodels.SharedViewModel}
 * pour observer et manipuler la liste des véhicules favoris de manière synchronisée
 * avec les autres fragments de l'application. Il évite les appels API redondants en
 * s'appuyant sur le cache centralisé du ViewModel.</p>
 *
 * <h3>Gestion des permissions :</h3>
 * <p>Le fragment gère automatiquement les demandes de permissions pour :
 * <ul>
 *     <li><strong>Appareil photo :</strong> {@code CAMERA} pour prendre une photo de profil</li>
 *     <li><strong>Localisation :</strong> {@code ACCESS_FINE_LOCATION} pour afficher
 *         la ville/région actuelle</li>
 * </ul>
 * </p>
 *
 * <h3>Synchronisation des données :</h3>
 * <p>Le fragment observe la liste {@code favoriteList} du {@link SharedViewModel}
 * et met automatiquement à jour l'affichage lorsque l'état des favoris change
 * depuis d'autres fragments (ex: {@link HistoryFragment}). Les données sont
 * chargées une seule fois via {@code loadDataIfNeeded()} pour optimiser les
 * performances et réduire les appels API.</p>
 *
 * <h3>Exemple d'utilisation dans une activité :</h3>
 * <pre>{@code
 * FragmentManager fm = getSupportFragmentManager();
 * FragmentTransaction ft = fm.beginTransaction();
 * ft.replace(R.id.container, new ProfileFragment());
 * ft.commit();
 * }</pre>
 *
 * @see SharedViewModel
 * @see VehicleAdapter
 * @see Photo
 * @see HistoryFragment
 */
public class ProfileFragment extends Fragment {

    /**
     * L'{@link ImageView} affichant la photo de profil de l'utilisateur.
     *
     * <p>Permet à l'utilisateur de modifier sa photo de profil en cliquant dessus,
     * ce qui déclenche l'ouverture d'un sélecteur (appareil photo ou galerie)
     * via la classe utilitaire {@link Photo}.</p>
     */
    private ImageView ivProfile;

    /**
     * L'{@link ImageView} représentant le bouton de déconnexion.
     *
     * <p>Un clic sur cette icône déclenche la déconnexion de l'utilisateur,
     * efface la session via {@link SessionManager} et redirige vers
     * {@link SignInActivity}.</p>
     */
    private ImageView ivLogout;

    /**
     * Le {@link TextView} affichant le nom d'utilisateur.
     *
     * <p>Actuellement configuré avec une valeur statique "Username".
     * Dans une implémentation complète, cette valeur devrait être récupérée
     * depuis les données utilisateur du {@link SharedViewModel}.</p>
     */
    private TextView tvUsername;

    /**
     * Le {@link TextView} affichant l'adresse email de l'utilisateur.
     *
     * <p>Actuellement configuré avec une valeur statique "email@example.com".
     * Dans une implémentation complète, cette valeur devrait être récupérée
     * depuis les données utilisateur du {@link SharedViewModel}.</p>
     */
    private TextView tvEmail;

    /**
     * Le {@link TextView} affichant la localisation actuelle de l'utilisateur.
     *
     * <p>Affiche la ville et la région basées sur les coordonnées GPS obtenues
     * via {@link FusedLocationProviderClient}. Si la permission de localisation
     * n'est pas accordée, affiche une valeur par défaut.</p>
     */
    private TextView tvCountry;

    /**
     * Lanceur de demande de permission pour l'utilisation de l'appareil photo.
     *
     * <p>Utilisé par la classe {@link Photo} pour demander la permission
     * {@code CAMERA} avant d'ouvrir l'appareil photo. En cas de refus,
     * un message d'erreur est affiché à l'utilisateur.</p>
     */
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    /**
     * Lanceur de demande de permission pour l'accès à la localisation.
     *
     * <p>Demande la permission {@code ACCESS_FINE_LOCATION} pour obtenir
     * les coordonnées GPS de l'utilisateur et afficher sa ville/région actuelle.
     * En cas de refus, un message d'erreur est affiché.</p>
     */
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;

    /**
     * Lanceur pour l'ouverture de l'appareil photo.
     *
     * <p>Utilisé par la classe {@link Photo} pour capturer une nouvelle
     * photo de profil. Une fois la photo prise, elle est automatiquement
     * affichée dans {@link #ivProfile}.</p>
     */
    private ActivityResultLauncher<Uri> cameraLauncher;

    /**
     * Lanceur pour l'ouverture de la galerie de photos.
     *
     * <p>Permet à l'utilisateur de sélectionner une image existante depuis
     * la galerie pour l'utiliser comme photo de profil. L'image sélectionnée
     * est immédiatement affichée dans {@link #ivProfile}.</p>
     */
    private ActivityResultLauncher<String> galleryLauncher;

    /**
     * Instance de la classe utilitaire {@link Photo} pour la gestion des images.
     *
     * <p>Encapsule la logique de gestion des photos de profil, incluant :
     * <ul>
     *     <li>Les demandes de permission</li>
     *     <li>L'ouverture de l'appareil photo</li>
     *     <li>La sélection depuis la galerie</li>
     *     <li>L'affichage du sélecteur de source (caméra/galerie)</li>
     * </ul>
     * </p>
     */
    private Photo photo;

    /**
     * Le {@link RecyclerView} affichant la liste des véhicules favoris de l'utilisateur.
     *
     * <p>Utilise un {@link VehicleAdapter} pour représenter chaque véhicule favori
     * et permet à l'utilisateur de voir les détails ou de supprimer des favoris
     * directement depuis le profil. La liste est automatiquement synchronisée
     * avec les changements effectués dans d'autres fragments.</p>
     */
    private RecyclerView recyclerView;

    /**
     * Client de localisation Google Play Services pour obtenir la position GPS.
     *
     * <p>Utilisé pour récupérer la dernière position connue de l'utilisateur
     * et la convertir en nom de ville/région via {@link Geocoder}.
     * Nécessite la permission {@code ACCESS_FINE_LOCATION}.</p>
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Code de demande de permission pour l'accès à la localisation.
     *
     * <p>Constante utilisée pour identifier les demandes de permission
     * de localisation dans les callbacks de résultat. Actuellement non
     * utilisée car la gestion se fait via {@link #requestLocationPermissionLauncher}.</p>
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    /**
     * L'adaptateur {@link VehicleAdapter} responsable de l'affichage des véhicules favoris.
     *
     * <p>Configure les listeners pour gérer :
     * <ul>
     *     <li>Le clic sur un véhicule (ouverture des détails)</li>
     *     <li>Le clic sur l'icône favori (suppression du favori)</li>
     * </ul>
     * La logique de gestion des favoris est déléguée au {@link SharedViewModel}.</p>
     */
    private VehicleAdapter adapter;

    /**
     * Le {@link SharedViewModel} partagé entre tous les fragments de l'application.
     *
     * <p>Centralise la gestion des données des véhicules et des favoris,
     * assurant la synchronisation entre {@link ProfileFragment} et
     * {@link HistoryFragment}. Évite les appels API redondants grâce
     * à son système de cache intégré.</p>
     */
    private SharedViewModel sharedViewModel;

    /**
     * Gestionnaire de session pour les opérations de connexion/déconnexion.
     *
     * <p>Utilisé pour effacer les données de session lors de la déconnexion
     * de l'utilisateur et gérer l'état d'authentification de l'application.</p>
     */
    private SessionManager sessionManager;

    /**
     * Initialise les lanceurs de permissions et les utilitaires nécessaires au fragment.
     *
     * <p>Cette méthode configure :
     * <ul>
     *     <li>Les lanceurs de demande de permissions (localisation et appareil photo)</li>
     *     <li>Les lanceurs pour l'appareil photo et la galerie</li>
     *     <li>L'instance de la classe utilitaire {@link Photo}</li>
     *     <li>Le client de localisation {@link FusedLocationProviderClient}</li>
     * </ul>
     * </p>
     *
     * <p>Les lanceurs sont configurés avec des callbacks appropriés pour gérer
     * les réponses de l'utilisateur (accord/refus de permission) et afficher
     * des messages d'erreur en cas de refus des permissions nécessaires.</p>
     *
     * @param savedInstanceState l'état sauvegardé du fragment, s'il existe ;
     *                          peut être {@code null} lors de la première création
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
     * Crée et configure la vue du fragment avec tous ses composants UI.
     *
     * <p>Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li><strong>Inflation du layout :</strong> charge {@code fragment_profile.xml}</li>
     *     <li><strong>Configuration des vues :</strong> photo de profil, bouton déconnexion,
     *         informations utilisateur</li>
     *     <li><strong>Initialisation du RecyclerView :</strong> configuration de l'adaptateur
     *         et des listeners pour les véhicules favoris</li>
     *     <li><strong>Configuration des observers :</strong> écoute les changements de la liste
     *         des favoris depuis le {@link SharedViewModel}</li>
     *     <li><strong>Chargement des données :</strong> déclenche le chargement des données
     *         si nécessaire via le cache du ViewModel</li>
     *     <li><strong>Récupération de la localisation :</strong> demande la position GPS
     *         pour afficher la ville/région</li>
     * </ol>
     * </p>
     *
     * <p>L'adaptateur est configuré avec deux listeners :
     * <ul>
     *     <li><strong>Clic sur véhicule :</strong> ouvre un dialog de détails via
     *         {@link VehicleDetailDialogFragment}</li>
     *     <li><strong>Clic sur favori :</strong> délègue au {@link SharedViewModel}
     *         pour synchroniser les changements</li>
     * </ul>
     * </p>
     *
     * @param inflater           le {@link LayoutInflater} pour gonfler la vue du fragment
     * @param container          le {@link ViewGroup} parent dans lequel la vue sera insérée ;
     *                          peut être {@code null}
     * @param savedInstanceState l'état sauvegardé du fragment ; peut être {@code null}
     * @return la vue racine du fragment configurée et prête à être affichée
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

        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText("Username");
        tvEmail = view.findViewById(R.id.tvEmail);
        tvEmail.setText("email@example.com");
        tvCountry = view.findViewById(R.id.tvCountry);

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
                vehicle -> sharedViewModel.toggleFavorite(vehicle)
        );
        recyclerView.setAdapter(adapter);

        sharedViewModel.getFavoriteList().observe(getViewLifecycleOwner(), favorites -> {
            adapter.updateList(favorites);
        });

        sharedViewModel.loadDataIfNeeded();

        return view;
    }

    /**
     * Récupère et affiche la localisation actuelle de l'utilisateur.
     *
     * <p>Cette méthode vérifie d'abord si la permission {@code ACCESS_FINE_LOCATION}
     * est accordée. Si ce n'est pas le cas, elle déclenche une demande de permission
     * via {@link #requestLocationPermissionLauncher}.</p>
     *
     * <p>Si la permission est accordée, elle utilise {@link FusedLocationProviderClient}
     * pour obtenir la dernière position connue et la convertit en nom de ville/région
     * via {@link #getCityName(double, double)}. En cas d'échec (pas de position disponible
     * ou permission refusée), une valeur par défaut est affichée.</p>
     *
     * <p><strong>Note :</strong> cette méthode est appelée automatiquement lors de
     * la création de la vue et peut également être déclenchée par l'accord de permission
     * de localisation.</p>
     */
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

    /**
     * Convertit des coordonnées GPS en nom de ville et région lisible.
     *
     * <p>Cette méthode utilise {@link Geocoder} pour effectuer une géolocalisation inverse
     * et obtenir des informations d'adresse à partir des coordonnées latitude/longitude.
     * Elle privilégie l'affichage dans l'ordre de priorité suivant :
     * <ol>
     *     <li><strong>Ville + Région :</strong> ex. "Paris, Île-de-France"</li>
     *     <li><strong>Sous-localité + Région :</strong> ex. "Montmartre, Île-de-France"</li>
     *     <li><strong>Région + Pays :</strong> ex. "Île-de-France, France"</li>
     *     <li><strong>Pays seulement :</strong> ex. "France"</li>
     * </ol>
     * </p>
     *
     * <p>En cas d'erreur (pas de connexion Internet, coordonnées invalides, etc.),
     * la méthode retourne une valeur par défaut obtenue depuis les ressources string.</p>
     *
     * <p><strong>Note :</strong> cette méthode effectue une opération réseau et peut
     * donc prendre quelques secondes à s'exécuter. Elle est appelée de manière
     * asynchrone depuis {@link #getLastLocation()}.</p>
     *
     * @param latitude  la latitude GPS en degrés décimaux
     * @param longitude la longitude GPS en degrés décimaux
     * @return une chaîne formatée représentant la localisation (ville, région, pays)
     *         ou une valeur par défaut en cas d'erreur
     */
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
}
