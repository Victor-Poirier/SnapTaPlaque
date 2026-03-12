
package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.ArrayList;

/**
 * Fragment dédié à l'affichage de l'historique complet des véhicules scannés.
 *
 * <p>Ce fragment présente à l'utilisateur la liste de tous les véhicules dont la plaque
 * d'immatriculation a été scannée ou saisie via le {@link SearchFragment}. Chaque
 * élément de la liste affiche les informations du véhicule ainsi qu'une icône étoile
 * permettant de le marquer ou démarquer comme favori.</p>
 *
 * <p>Le fragment observe le flux {@link SharedViewModel#getVehicleList()} pour se mettre
 * à jour automatiquement lorsqu'un nouveau véhicule est ajouté à l'historique ou
 * lorsque l'état favori d'un véhicule est modifié (depuis ce fragment ou depuis
 * le {@link ProfileFragment}).</p>
 *
 * <p>L'interaction favori est déléguée au {@link SharedViewModel#toggleFavorite(com.example.snaptaplaque.models.Vehicle)}
 * via le callback du {@link VehicleAdapter}, garantissant la synchronisation des données
 * entre tous les fragments partageant le même ViewModel.</p>
 *
 * @see SharedViewModel#getVehicleList()
 * @see SharedViewModel#toggleFavorite(com.example.snaptaplaque.models.Vehicle)
 * @see ProfileFragment
 * @see SearchFragment
 * @see VehicleAdapter
 */
public class HistoryFragment extends Fragment {

    /**
     * RecyclerView affichant la liste complète des véhicules scannés (historique).
     */
    private RecyclerView recyclerView;

    /**
     * Adapteur gérant l'affichage des véhicules dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * ViewModel partagé avec les autres fragments pour centraliser les données véhicules et profil.
     */
    private SharedViewModel sharedViewModel;

    /**
     * Gonfle la vue du fragment et configure l'ensemble des composants graphiques.
     *
     * <p>Cette méthode effectue les opérations suivantes dans l'ordre :
     * <ol>
     *     <li>Gonfle le layout {@code fragment_history.xml}</li>
     *     <li>Initialise le {@link RecyclerView} avec un {@link LinearLayoutManager}
     *         pour un défilement vertical</li>
     *     <li>Récupère le {@link SharedViewModel} scopé à l'activité parente
     *         ({@link com.example.snaptaplaque.activities.MainActivity})</li>
     *     <li>Crée le {@link VehicleAdapter} avec une liste initiale vide et un callback
     *         qui délègue l'inversion de l'état favori au ViewModel via
     *         {@link SharedViewModel#toggleFavorite(com.example.snaptaplaque.models.Vehicle)}</li>
     *     <li>Observe la liste complète des véhicules ({@link SharedViewModel#getVehicleList()})
     *         pour mettre à jour automatiquement l'affichage à chaque ajout de véhicule
     *         ou modification de l'état favori</li>
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        adapter = new VehicleAdapter(
                new ArrayList<>(),
                vehicle -> {
                    VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.newInstance(vehicle.getDetails());
                    dialog.show(getChildFragmentManager(), "detail");
                },

                vehicle -> sharedViewModel.toggleFavorite(vehicle)
        );
        recyclerView.setAdapter(adapter);

        // Met à jour la liste automatiquement quand un véhicule est ajouté ou modifié
        sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            adapter.updateList(vehicles);
        });

        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Met à jour la requête dans le ViewModel
                sharedViewModel.setSearchQuery(newText);
                // Filtre et affiche la liste
                adapter.updateList(sharedViewModel.getFilteredVehicles());
                return true;
            }
        });

        // lors d'un ajout de véhicule en arrière-plan
        sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            adapter.updateList(sharedViewModel.getFilteredVehicles());
        });

        return view;
    }
}