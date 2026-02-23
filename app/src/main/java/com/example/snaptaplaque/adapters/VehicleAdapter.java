package com.example.snaptaplaque.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Vehicle;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        // Ici, on doit créer une vue pour chaque élément de la liste (ex: un layout XML pour afficher les détails du véhicule)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);

        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position){
        Vehicle vehicle = vehicleList.get(position);

        // On remplit les données dynamiques
        holder.tvImmatriculation.setText(vehicle.getImmatriculation());
        holder.tvDetails.setText(vehicle.getDetails());

        // Gestion de l'état "favori"
        if(vehicle.isFavorite()){
            holder.ivFavorite.setImageResource(R.drawable.ic_star); // Icône "favori"
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_star_outline); // Icône "non favori"
        }
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    // Le ViewHolder qui contient les références aux vues de chaque élément
    public static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvImmatriculation, tvDetails;
        ImageView ivFavorite, ivCar;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImmatriculation = itemView.findViewById(R.id.tvImmatriculation);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            ivCar = itemView.findViewById(R.id.ivVehicle);
        }
    }


}
