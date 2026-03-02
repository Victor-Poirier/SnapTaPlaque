package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.VehicleAdapter;
import com.example.snaptaplaque.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsable de l'affichage de l'historique des plaques d'immatriculation scannées.
 * <p>
 * Ce fragment présente une liste scrollable ({@link RecyclerView}) de véhicules
 * précédemment scannés par l'utilisateur. Chaque élément de la liste contient :
 * <ul>
 *     <li>Le numéro d'immatriculation du véhicule</li>
 *     <li>Les détails descriptifs (marque, modèle, motorisation, etc.)</li>
 *     <li>Le statut favori du véhicule</li>
 * </ul>
 * <p>
 * <strong>Note :</strong> Actuellement, les données affichées sont fictives et servent
 * uniquement à des fins de test. Elles devront être remplacées par une source de données
 * persistante (base de données locale, API distante, etc.).
 * </p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see Fragment
 * @see VehicleAdapter
 * @see Vehicle
 */
public class HistoryFragment extends Fragment {
    /*
     * Ce fragment affichera l'historique des plaques scannées par l'utilisateur.
     * Il peut inclure une liste (RecyclerView) des plaques avec des détails comme :
     * - Date et heure du scan
     * - Image de la plaque
     * - Informations associées (marque, modèle, etc.)
     *
     * Le fragment peut aussi permettre de filtrer ou trier l'historique.
     */

    /**
     * Le {@link RecyclerView} utilisé pour afficher la liste des véhicules scannés.
     */
    private RecyclerView recyclerView;

    /**
     * L'adaptateur gérant l'affichage des objets {@link Vehicle} dans le {@link RecyclerView}.
     */
    private VehicleAdapter adapter;

    /**
     * Initialise et retourne la vue associée à ce fragment.
     * <p>
     * Cette méthode gonfle le layout {@code fragment_history}, configure le
     * {@link RecyclerView} avec un {@link LinearLayoutManager} vertical, puis
     * peuple la liste avec un jeu de données fictif à des fins de démonstration.
     * </p>
     *
     * @param inflater          le {@link LayoutInflater} utilisé pour gonfler la vue du fragment
     * @param container         le {@link ViewGroup} parent auquel la vue sera rattachée,
     *                          ou {@code null} si aucun parent n'est disponible
     * @param savedIntanceState le {@link Bundle} contenant l'état précédemment sauvegardé
     *                          du fragment, ou {@code null} s'il s'agit d'une première création
     * @return la {@link View} racine du fragment contenant la liste des véhicules
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedIntanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.rvVehicles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        /*On crée une liste fictive pour tester*/
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("DR-593-DE", "Renault Laguna 3 Coupé Intens 2.0 dCi 175", true));
        vehicles.add(new Vehicle("AB-123-CD", "Peugeot 208 1.2 PureTech 110", false));
        vehicles.add(new Vehicle("EF-456-GH", "Citroën C3 Aircross Shine 1.5 BlueHDi 120", true));
        vehicles.add(new Vehicle("IJ-789-KL", "Volkswagen Golf 7 1.4 TSI 150", false));
        vehicles.add(new Vehicle("MN-012-OP", "Ford Fiesta 1.0 EcoBoost 100", true));

        adapter = new VehicleAdapter(vehicles);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
