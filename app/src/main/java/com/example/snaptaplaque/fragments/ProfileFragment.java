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
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.FavoritesCall;
import com.example.snaptaplaque.network.apicall.response.ApiResponseFavorites;
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
    private Photo photo;
    private RecyclerView recyclerView;

    /**
     * Adapteur gérant l'affichage des véhicules favoris dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * ViewModel partagé avec les autres fragments pour centraliser les données véhicules et profil.
     */
    private SharedViewModel sharedViewModel;

    private ApiService apiService;

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

        // Même singleton Retrofit que dans LaunchActivity
        apiService = ApiClient.getRetrofit().create(ApiService.class);

        // Outil qui ouvre la permission d'utiliser la caméra
        requestPermissionLauncher = registerForActivityResult(
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
        photo = new Photo(requireContext(), requestPermissionLauncher, cameraLauncher, galleryLauncher);
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
        ivProfile.setOnClickListener(v -> photo.showChoice());

        ivLogout = view.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(null);

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

    // Endpoint : /v1/favorites/all
    public void getAllFavorites(){return;}

    // Endpoint : /v1/favorites/remove
    public void removeFavorite(){
        FavoritesCall.removeFavorite(apiService, new FavoritesRemoveRequest(""), new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {
                // Mettre à jour l'UI : afficher succès
            }

            @Override
            public void onResponseFailure(String message) {
                // Mettre à jour l'UI : afficher erreur
            }

            @Override
            public void onCallFailure(Throwable t) {
                // Mettre à jour l'UI : afficher erreur
            }
        }, new ApiResponseFavorites() {
            @Override
            public void RemoveResponse(FavoritesRemoveResponse favoritesRemoveResponse) {

            }
        });
    }

    // Endpoint : /v1/account/me
    public void getUserInfo(){return;}

    // Endpoint : /v1/predictions/stats
    public void userStat(){return;}


}
