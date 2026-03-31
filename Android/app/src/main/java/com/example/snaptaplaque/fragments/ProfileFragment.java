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
 *
 * <p>Ce fragment permet à l'utilisateur de :
 * <ul>
 * <li>Visualiser ses informations personnelles (nom, email, localisation actuelle)</li>
 * <li>Changer sa photo de profil via la caméra ou la galerie</li>
 * <li>Consulter sa liste de véhicules favoris via un {@link RecyclerView}</li>
 * <li>Se déconnecter ou accéder aux informations complémentaires de l'API</li>
 * </ul>
 * </p>
 *
 * <p>La gestion des données est synchronisée avec le {@link SharedViewModel} pour les favoris
 * et le {@link SessionManager} pour l'état de la session.</p>
 *
 * @see com.example.snaptaplaque.models.Photo
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 * @see com.example.snaptaplaque.adapters.VehicleAdapter
 */
public class ProfileFragment extends Fragment {

    private ImageView ivProfile;
    private ImageView ivLogout;
    private ImageView ivApiInfo;
    private TextView tvUsername;
    private TextView tvEmail;
    private TextView tvCountry;

    /** Launcher pour la demande de permission caméra. */
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    /** Launcher pour la demande de permission de localisation. */
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;

    /** Launcher pour le résultat du contrat de capture photo. */
    private ActivityResultLauncher<Uri> cameraLauncher;

    /** Launcher pour le résultat de la sélection en galerie. */
    private ActivityResultLauncher<String> galleryLauncher;

    /** Classe utilitaire pour la gestion des médias (Caméra/Galerie). */
    private Photo photo;
    private RecyclerView recyclerView;

    /** Client Google Play Services pour la géolocalisation. */
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    /** Adaptateur pour la liste des véhicules favoris. */
    private VehicleAdapter adapter;

    /** ViewModel partagé pour observer la liste des favoris. */
    private SharedViewModel sharedViewModel;

    /** Gestionnaire de session pour la déconnexion. */
    private SessionManager sessionManager;

    /**
     * Initialise le fragment et enregistre les launchers pour les permissions et les activités de capture.
     *
     * @param savedInstanceState État sauvegardé du fragment.
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

    /**
     * Gonfle la vue du fragment et initialise les composants graphiques et les listeners.
     *
     * @param inflater Le {@link LayoutInflater}.
     * @param container Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue {@link View} du fragment.
     */
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

    /**
     * Tente de récupérer la dernière localisation connue de l'utilisateur.
     * <p>Si la permission n'est pas accordée, demande la permission {@code ACCESS_FINE_LOCATION}.</p>
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
     * Convertit des coordonnées GPS en une chaîne de caractères localisée (Ville, Région).
     *
     * @param latitude  La latitude de la position.
     * @param longitude La longitude de la position.
     * @return Une chaîne formatée (ex: "Paris, Île-de-France") ou la valeur par défaut du pays.
     */
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

    /**
     * Récupère les informations textuelles (Profil) et l'image de profil depuis le serveur.
     *
     * <p>Cette méthode orchestre deux appels API distincts et asynchrones :
     * <ul>
     * <li>{@link AccountCall#me} : Pour les données textuelles (Username, Email).</li>
     * <li>{@link AccountCall#profilePicture} : Pour le flux binaire de l'image de profil.</li>
     * </ul>
     * </p>
     */
    public void getUserInfo(){
        AccountCall.me(new ApiCallback() {
            /**
             * Met à jour les champs textuels du profil avec les données reçues.
             *
             * @param response Objet {@link Response} contenant le corps {@link MeResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                MeResponse res = (MeResponse)response.body();
                tvUsername.setText(res.getUsername());
                tvEmail.setText(res.getEmail());
            }

            /**
             * Gère l'échec de la récupération des données textuelles.
             * <p>Redirige vers {@link SignInActivity} si la session a expiré.</p>
             *
             * @param response Réponse d'erreur du serveur.
             */
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
            /**
             * Décode le flux binaire reçu en {@link Bitmap} et l'affiche.
             *
             * @param response Objet {@link Response} contenant le {@link ResponseBody}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                ResponseBody body = (ResponseBody) response.body();
                Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                ivProfile.setImageBitmap(bitmap);
            }

            /**
             * Gère l'échec de récupération de l'image.
             *
             * @param response Réponse d'erreur du serveur.
             */
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
     * Prépare un fichier temporaire optimisé à partir d'une URI pour l'upload.
     *
     * @param uri L'{@link Uri} de l'image source.
     * @return Un objet {@link java.io.File} pointant vers l'image compressée ou {@code null}.
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

    /**
     * Envoie la nouvelle photo de profil au serveur via une requête Multipart.
     *
     * <p>Cette méthode orchestre l'upload en trois phases :
     * <ol>
     * <li>Vérification de la validité de l'{@link Uri}.</li>
     * <li>Compression et conversion de l'image en {@link java.io.File} via {@link #prepareImageFile(Uri)}.</li>
     * <li>Construction du corps de requête {@link okhttp3.MultipartBody.Part} pour l'envoi asynchrone.</li>
     * </ol>
     * </p>
     *
     * @param uri L'{@link Uri} de l'image sélectionnée ou capturée.
     * @see AccountCall#changeProfilePicture
     * @see #prepareImageFile(Uri)
     */
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
            /**
             * Traite la confirmation de mise à jour de la photo par le serveur.
             *
             * @param response Objet {@link Response} indiquant le succès de l'upload.
             */
            @Override
            public void onResponseSuccess(Response response) {
            }

            /**
             * Gère les échecs de réponse du serveur (ex: fichier trop lourd ou jeton expiré).
             *
             * @param response La réponse d'erreur contenant le code HTTP.
             */
            @Override
            public void onResponseFailure(Response response) {
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            /**
             * Gère les erreurs de connexion ou les interruptions durant le transfert du fichier.
             *
             * @param t L'exception {@link Throwable} décrivant l'échec de l'appel.
             */
            @Override
            public void onCallFailure(Throwable t) {
            }
        }, body);
    }
}