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

    private RecyclerView recyclerView;
    private VehicleAdapter adapter; // Adapter pour afficher les véhicules dans le RecyclerView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedIntanceState){
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
