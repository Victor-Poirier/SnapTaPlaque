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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.ArrayList;

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
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri tempImageUri;
    private RecyclerView recyclerView;

    /**
     * Adapteur gérant l'affichage des véhicules favoris dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * ViewModel partagé avec les autres fragments pour centraliser les données véhicules et profil.
     */
    private SharedViewModel sharedViewModel;

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
                        ivProfile.setImageURI(null);
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

        ivProfile = view.findViewById(R.id.ivProfilePicture);
        ivProfile.setOnClickListener(v -> showChoice());

        ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(null);

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Affiche uniquement les favoris
        adapter = new VehicleAdapter(
                new ArrayList<>(),
                vehicle -> sharedViewModel.toggleFavorite(vehicle)
        );
        recyclerView.setAdapter(adapter);

        // Se met à jour automatiquement quand les favoris changent
        sharedViewModel.getFavoriteList().observe(getViewLifecycleOwner(), favorites -> {
            adapter.updateList(favorites);
        });

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
