package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment représentant le profil de l'utilisateur.
 * <p>
 * Ce fragment gère deux responsabilités principales :
 * <ol>
 *     <li><strong>Photo de profil :</strong> Permet à l'utilisateur de sélectionner une image
 *     depuis la galerie de son appareil via un {@link ActivityResultLauncher}. L'image
 *     choisie est ensuite affichée dans un {@link ImageView} dédié.</li>
 *     <li><strong>Liste des véhicules favoris :</strong> Affiche dans un {@link RecyclerView}
 *     la liste des véhicules marqués comme favoris par l'utilisateur, incluant :
 *         <ul>
 *             <li>Le numéro d'immatriculation</li>
 *             <li>Le modèle du véhicule</li>
 *             <li>Le statut favori</li>
 *         </ul>
 *     </li>
 * </ol>
 * </p>
 *
 * <h3>Cycle de vie</h3>
 * <ul>
 *     <li>{@link #onCreate(Bundle)} — Enregistre le lanceur de sélection d'image
 *     ({@link ActivityResultContracts.GetContent}) avant la création de la vue.</li>
 *     <li>{@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} — Gonfle le layout,
 *     configure les listeners et peuple le {@link RecyclerView}.</li>
 * </ul>
 *
 * <p><strong>Note :</strong> Les données de véhicules affichées sont actuellement fictives
 * (placeholders) et devront être remplacées par une source de données persistante
 * (Room, Firebase, API REST, etc.) dans une version ultérieure.</p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see Fragment
 * @see VehicleAdapter
 * @see Vehicle
 * @see ActivityResultLauncher
 */
public class ProfileFragment extends Fragment {

    /**
     * L'{@link ImageView} affichant la photo de profil de l'utilisateur.
     */
    private ImageView ivProfile;

    /**
     * Le lanceur d'activité permettant d'ouvrir la galerie d'images de l'appareil.
     * <p>
     * Utilise le contrat {@link ActivityResultContracts.GetContent} pour filtrer
     * uniquement les fichiers de type {@code image/*}. Le résultat (URI de l'image
     * sélectionnée) est appliqué directement à {@link #ivProfile}.
     * </p>
     */
    private ActivityResultLauncher<String> imagePickerLauncher;

    /**
     * Le {@link RecyclerView} utilisé pour afficher la liste des véhicules favoris.
     */
    private RecyclerView recyclerView;

    /**
     * L'adaptateur gérant le binding des objets {@link Vehicle} dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * Appelé lors de la création initiale du fragment.
     * <p>
     * Enregistre le {@link ActivityResultLauncher} pour la sélection d'image depuis
     * la galerie. Cette opération doit être effectuée avant {@code onCreateView} afin
     * de respecter les contraintes du cycle de vie des {@link Fragment}.
     * </p>
     *
     * @param savedInstanceState le {@link Bundle} contenant l'état précédemment sauvegardé
     *                           du fragment, ou {@code null} lors d'une première création
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Outil qui va chercher l'image dans le téléphone
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Affiche l'image choisie par l'utilisateur
                        ivProfile.setImageURI(uri);
                    }
                }
        );
    }

    /**
     * Initialise et retourne la hiérarchie de vues associée à ce fragment.
     * <p>
     * Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li>Gonfle le layout {@code fragment_profile}.</li>
     *     <li>Récupère la référence de l'{@link ImageView} de profil et lui attache
     *     un {@link View.OnClickListener} qui déclenche l'ouverture de la galerie.</li>
     *     <li>Configure le {@link RecyclerView} avec un {@link LinearLayoutManager}
     *     vertical.</li>
     *     <li>Crée une liste fictive de {@link Vehicle} (placeholders) et l'injecte
     *     dans le {@link VehicleAdapter}.</li>
     * </ol>
     * </p>
     *
     * @param inflater          le {@link LayoutInflater} utilisé pour gonfler la vue du fragment
     * @param container         le {@link ViewGroup} parent auquel la vue sera éventuellement
     *                          rattachée, ou {@code null} si aucun parent n'est disponible
     * @param savedIntanceState le {@link Bundle} contenant l'état précédemment sauvegardé
     *                          du fragment, ou {@code null} lors d'une première création
     * @return la {@link View} racine du fragment contenant le profil utilisateur et la
     * liste de véhicules
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedIntanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfile = view.findViewById(R.id.ivProfilePicture);

        // Lance la galerie quand on clique sur la photo
        ivProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));
        vehicles.add(new Vehicle("Plaque d'immatriculation", "Modèle", true));

        adapter = new VehicleAdapter(vehicles);
        recyclerView.setAdapter(adapter);

        return view;
    }
}