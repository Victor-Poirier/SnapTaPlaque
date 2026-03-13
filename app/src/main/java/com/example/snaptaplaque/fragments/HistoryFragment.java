package com.example.snaptaplaque.fragments;

import android.content.Intent;
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
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.utils.SessionManager;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.ArrayList;

/**
 * Fragment responsable de l'affichage et de la gestion de l'historique des véhicules
 * consultés par l'utilisateur.
 *
 * <p>Ce fragment présente la liste complète de tous les véhicules que l'utilisateur
 * a consultés précédemment, avec les fonctionnalités suivantes :
 * <ul>
 *     <li>Affichage de l'historique complet des véhicules via un {@link RecyclerView}</li>
 *     <li>Recherche et filtrage en temps réel par immatriculation, marque ou modèle</li>
 *     <li>Gestion des favoris avec synchronisation automatique</li>
 *     <li>Ouverture des détails de véhicule via un dialog</li>
 * </ul>
 * </p>
 *
 * <p>Le fragment utilise le {@link SharedViewModel} pour observer et manipuler
 * la liste des véhicules de manière synchronisée avec les autres fragments de
 * l'application, notamment le {@link ProfileFragment}. Il évite les appels API
 * redondants en s'appuyant sur le cache centralisé du ViewModel.</p>
 *
 * <h3>Gestion du cache et des données :</h3>
 * <p>Le fragment ne fait jamais d'appels API directs. Au lieu de cela :
 * <ol>
 *     <li>Il appelle {@link SharedViewModel#loadDataIfNeeded()} qui charge les données
 *         uniquement si elles ne sont pas déjà en cache</li>
 *     <li>Il observe {@code vehicleList} via {@link SharedViewModel#getVehicleList()}
 *         pour recevoir automatiquement les mises à jour</li>
 *     <li>Les changements d'état des favoris sont synchronisés automatiquement
 *         avec le {@link ProfileFragment} via le ViewModel partagé</li>
 * </ol>
 * </p>
 *
 * <h3>Fonctionnalité de recherche :</h3>
 * <p>Le fragment intègre une {@link androidx.appcompat.widget.SearchView} qui permet
 * de filtrer les véhicules en temps réel. La recherche s'effectue sur :
 * <ul>
 *     <li>Le numéro d'immatriculation</li>
 *     <li>La marque du véhicule</li>
 *     <li>Le modèle du véhicule</li>
 * </ul>
 * La logique de filtrage est déléguée au {@link SharedViewModel#setSearchQuery(String)}
 * et {@link SharedViewModel#getFilteredVehicles()}.</p>
 *
 * <h3>Performance et optimisations :</h3>
 * <p>Ce fragment est optimisé pour minimiser les appels API :
 * <ul>
 *     <li><strong>Pas d'appels redondants :</strong> utilise le système de cache du ViewModel</li>
 *     <li><strong>Chargement unique :</strong> les données ne sont chargées qu'une seule fois
 *         par session, même en naviguant entre les onglets</li>
 *     <li><strong>Synchronisation efficace :</strong> partage les données avec
 *         {@link ProfileFragment} sans duplication</li>
 * </ul>
 * </p>
 *
 * @see SharedViewModel
 * @see VehicleAdapter
 * @see ProfileFragment
 * @see com.example.snaptaplaque.fragments.VehicleDetailDialogFragment
 */
public class HistoryFragment extends Fragment {

    /**
     * Le {@link RecyclerView} affichant la liste des véhicules de l'historique.
     *
     * <p>Configuré avec un {@link LinearLayoutManager} pour un affichage vertical
     * des éléments. Utilise un {@link VehicleAdapter} pour gérer l'affichage et
     * les interactions avec chaque véhicule de la liste.</p>
     */
    private RecyclerView recyclerView;

    /**
     * L'adaptateur {@link VehicleAdapter} responsable de l'affichage des véhicules.
     *
     * <p>Configure les listeners pour gérer :
     * <ul>
     *     <li>Le clic sur un véhicule : ouverture du dialog de détails</li>
     *     <li>Le clic sur l'icône favori : délégation au {@link SharedViewModel}</li>
     * </ul>
     * La liste affichée est automatiquement mise à jour via les observers
     * du {@link SharedViewModel}.</p>
     */
    private VehicleAdapter adapter;

    /**
     * Le {@link SharedViewModel} partagé entre tous les fragments de l'application.
     *
     * <p>Centralise la gestion des données des véhicules et des favoris,
     * assurant la synchronisation entre {@link HistoryFragment} et
     * {@link ProfileFragment}. Fournit également les fonctionnalités de
     * recherche et de filtrage via {@code getFilteredVehicles()}.</p>
     */
    private SharedViewModel sharedViewModel;

    /**
     * Gestionnaire de session pour les opérations d'authentification.
     *
     * <p>Utilisé pour vérifier l'état de connexion de l'utilisateur et
     * gérer les redirections si nécessaire. Actuellement initialisé mais
     * peu utilisé dans ce fragment.</p>
     */
    private SessionManager sessionManager;

    /**
     * Crée et configure la vue du fragment avec tous ses composants UI.
     *
     * <p>Cette méthode effectue les opérations suivantes dans l'ordre :
     * <ol>
     *     <li><strong>Initialisation des utilitaires :</strong> {@link SessionManager}</li>
     *     <li><strong>Inflation du layout :</strong> charge {@code fragment_history.xml}</li>
     *     <li><strong>Configuration du RecyclerView :</strong> layout manager et adaptateur</li>
     *     <li><strong>Configuration de l'adaptateur :</strong> listeners pour les clics
     *         sur véhicules et favoris</li>
     *     <li><strong>Configuration des observers :</strong> écoute les changements de
     *         {@code vehicleList} depuis le {@link SharedViewModel}</li>
     *     <li><strong>Configuration de la recherche :</strong> {@link androidx.appcompat.widget.SearchView}
     *         avec filtrage en temps réel</li>
     *     <li><strong>Chargement des données :</strong> déclenche le chargement si
     *         nécessaire via le cache du ViewModel</li>
     * </ol>
     * </p>
     *
     * <p>Les listeners de l'adaptateur sont configurés pour :
     * <ul>
     *     <li><strong>Clic sur véhicule :</strong> ouvre un dialog
     *         {@link com.example.snaptaplaque.fragments.VehicleDetailDialogFragment}
     *         avec les détails complets du véhicule</li>
     *     <li><strong>Clic sur favori :</strong> délègue au
     *         {@link SharedViewModel#toggleFavorite(com.example.snaptaplaque.models.Vehicle)}
     *         pour maintenir la synchronisation avec le {@link ProfileFragment}</li>
     * </ul>
     * </p>
     *
     * <p>La {@link androidx.appcompat.widget.SearchView} est configurée pour filtrer
     * la liste en temps réel via {@link SharedViewModel#setSearchQuery(String)}.
     * Le filtrage s'effectue sur l'immatriculation, la marque et le modèle des véhicules.</p>
     *
     * <p><strong>Optimisation importante :</strong> ce fragment n'effectue qu'un seul
     * appel à {@code loadDataIfNeeded()}, qui ne déclenche des appels API que si les
     * données ne sont pas déjà en cache. Cela garantit des performances optimales
     * lors de la navigation entre onglets.</p>
     *
     * @param inflater           le {@link LayoutInflater} pour gonfler la vue du fragment
     * @param container          le {@link ViewGroup} parent dans lequel la vue sera insérée ;
     *                          peut être {@code null}
     * @param savedInstanceState l'état sauvegardé du fragment ; peut être {@code null}
     * @return la vue racine du fragment configurée et prête à être affichée,
     *         avec tous les composants initialisés et les listeners configurés
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sessionManager = new SessionManager(this.getContext());

        View view = inflater.inflate(R.layout.fragment_history, container, false);

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

        // Observer les changements
        sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            adapter.updateList(sharedViewModel.getFilteredVehicles());
        });

        // Configuration de la recherche
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sharedViewModel.setSearchQuery(query);
                adapter.updateList(sharedViewModel.getFilteredVehicles());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                sharedViewModel.setSearchQuery(newText);
                adapter.updateList(sharedViewModel.getFilteredVehicles());
                return true;
            }
        });

        // UN SEUL appel pour charger les données si nécessaire
        sharedViewModel.loadDataIfNeeded();

        return view;
    }

    public void comeBackLogin(){
        Intent intent = new Intent(this.getContext(), SignInActivity.class);
        startActivity(intent);
    }
}
