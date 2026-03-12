
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
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;;
import com.example.snaptaplaque.models.api.predictions.HistoryResponse;
import com.example.snaptaplaque.models.api.predictions.HistoryResult;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.FavoritesCall;
import com.example.snaptaplaque.network.apicall.PredictionsCall;
import com.example.snaptaplaque.utils.SessionManager;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

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

    private SessionManager sessionManager;

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
        // Même SharedPreferences ("snap_tap_plaque_session") que dans LaunchActivity
        sessionManager = new SessionManager(this.getContext());

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        adapter = new VehicleAdapter(
                new ArrayList<>(),
                vehicle -> {
                    VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                    dialog.show(getChildFragmentManager(), "vehicle_detail");
                },

                vehicle -> sharedViewModel.toggleFavorite(vehicle)
        );
        recyclerView.setAdapter(adapter);

        // Met à jour la liste automatiquement quand un véhicule est ajouté ou modifié
        sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            adapter.updateList(vehicles);
        });

        return view;
    }

    // Endpoint : /v1/predictions/history
    public void getHistory(){
        PredictionsCall.getHistory(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                HistoryResponse history = (HistoryResponse)response.body();
                List<HistoryResult> list = history.getHistory();

                for(HistoryResult v : list){
                    sharedViewModel.addVehicle(v.);
                }
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

    // Endpoint : /v1/favorites/add
    public void addFavorite(){
        FavoritesCall.addFavorite(new FavoritesAddRequest(""), new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                // Mettre à jour l'UI : afficher succès
            }

            @Override
            public void onResponseFailure(Response response) {
                // Mettre à jour l'UI : afficher erreur
            }

            @Override
            public void onCallFailure(Throwable t) {
                // Mettre à jour l'UI : afficher erreur
            }
        });

    }

    // Endpoint : /v1/favorites/remove
    public void removeFavorite(){
        FavoritesCall.removeFavorite(new FavoritesRemoveRequest(""), new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                // Mettre à jour l'UI : afficher succès
            }

            @Override
            public void onResponseFailure(Response response) {
                // Mettre à jour l'UI : afficher erreur
            }

            @Override
            public void onCallFailure(Throwable t) {
                // Mettre à jour l'UI : afficher erreur
            }
        });
    }
}