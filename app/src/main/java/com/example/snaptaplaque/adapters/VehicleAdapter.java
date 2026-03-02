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

/**
 * Adaptateur {@link RecyclerView.Adapter} responsable de l'affichage d'une liste
 * de {@link Vehicle} dans un {@link RecyclerView}.
 * <p>
 * Chaque élément de la liste est représenté par le layout {@code item_vehicle}
 * et affiche les informations suivantes :
 * <ul>
 *     <li>Le numéro d'immatriculation du véhicule</li>
 *     <li>Les détails descriptifs (marque, modèle, motorisation, etc.)</li>
 *     <li>Une icône indiquant le statut favori du véhicule
 *         ({@code ic_star} si favori, {@code ic_star_outline} sinon)</li>
 *     <li>Une image représentative du véhicule</li>
 * </ul>
 * </p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see RecyclerView.Adapter
 * @see VehicleViewHolder
 * @see Vehicle
 */
public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    /**
     * La liste des véhicules à afficher dans le {@link RecyclerView}.
     */
    private List<Vehicle> vehicleList;

    /**
     * Construit un nouvel adaptateur avec la liste de véhicules spécifiée.
     *
     * @param vehicleList la liste de {@link Vehicle} à afficher ;
     *                    ne doit pas être {@code null}
     */
    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    /**
     * Crée un nouveau {@link VehicleViewHolder} en gonflant le layout {@code item_vehicle}.
     * <p>
     * Appelé par le {@link RecyclerView} lorsqu'un nouveau ViewHolder est nécessaire
     * pour représenter un élément de la liste.
     * </p>
     *
     * @param parent   le {@link ViewGroup} parent dans lequel la nouvelle vue sera ajoutée
     * @param viewType le type de vue du nouvel élément (non utilisé ici, un seul type)
     * @return une nouvelle instance de {@link VehicleViewHolder} contenant la vue gonflée
     */
    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ici, on doit créer une vue pour chaque élément de la liste (ex: un layout XML pour afficher les détails du véhicule)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);

        return new VehicleViewHolder(view);
    }

    /**
     * Met à jour le contenu du {@link VehicleViewHolder} pour refléter les données
     * du {@link Vehicle} situé à la position spécifiée dans la liste.
     * <p>
     * Affecte le numéro d'immatriculation, les détails du véhicule et l'icône
     * de favori appropriée en fonction de l'état {@link Vehicle#isFavorite()}.
     * </p>
     *
     * @param holder   le {@link VehicleViewHolder} à mettre à jour
     * @param position la position de l'élément dans la liste {@link #vehicleList}
     */
    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        // On remplit les données dynamiques
        holder.tvImmatriculation.setText(vehicle.getImmatriculation());
        holder.tvDetails.setText(vehicle.getDetails());

        // Gestion de l'état "favori"
        if (vehicle.isFavorite()) {
            holder.ivFavorite.setImageResource(R.drawable.ic_star); // Icône "favori"
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_star_outline); // Icône "non favori"
        }
    }

    /**
     * Retourne le nombre total de véhicules dans la liste.
     *
     * @return le nombre d'éléments dans {@link #vehicleList}
     */
    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    /**
     * ViewHolder interne contenant les références aux vues de chaque élément
     * de la liste de véhicules.
     * <p>
     * Ce ViewHolder met en cache les vues du layout {@code item_vehicle}
     * afin d'éviter des appels répétés à {@link View#findViewById(int)}
     * et ainsi améliorer les performances de défilement du {@link RecyclerView}.
     * </p>
     *
     * @see RecyclerView.ViewHolder
     */
    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        /**
         * Le {@link TextView} affichant le numéro d'immatriculation du véhicule.
         */
        TextView tvImmatriculation;

        /**
         * Le {@link TextView} affichant les détails descriptifs du véhicule.
         */
        TextView tvDetails;

        /**
         * L'{@link ImageView} affichant l'icône de statut favori.
         */
        ImageView ivFavorite;

        /**
         * L'{@link ImageView} affichant l'image représentative du véhicule.
         */
        ImageView ivCar;

        /**
         * Construit un nouveau {@code VehicleViewHolder} et récupère les références
         * aux vues enfants du layout {@code item_vehicle}.
         *
         * @param itemView la vue racine de l'élément de la liste
         */
        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImmatriculation = itemView.findViewById(R.id.tvImmatriculation);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            ivCar = itemView.findViewById(R.id.ivVehicle);
        }
    }
}