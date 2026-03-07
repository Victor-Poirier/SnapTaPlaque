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
 *
 * <p>Chaque élément de la liste est représenté par le layout {@code item_vehicle}
 * et affiche les informations suivantes :
 * <ul>
 *     <li>Le numéro d'immatriculation du véhicule ({@link Vehicle#getImmatriculation()})</li>
 *     <li>Les détails descriptifs du véhicule — marque, modèle, motorisation, etc.
 *         ({@link Vehicle#getDetails()})</li>
 *     <li>Une icône étoile indiquant le statut favori du véhicule
 *         ({@code ic_star} si {@link Vehicle#isFavorite()} vaut {@code true},
 *         {@code ic_star_outline} sinon)</li>
 *     <li>Une image représentative du véhicule</li>
 * </ul>
 * </p>
 *
 * <p>Cet adaptateur supporte deux modes de fonctionnement pour la gestion des favoris :
 * <ol>
 *     <li><strong>Avec listener :</strong> lorsqu'un {@link OnFavoriteClickListener} est fourni
 *         via le constructeur {@link #VehicleAdapter(List, OnVehicleClickListener, OnFavoriteClickListener)}, le clic
 *         sur l'icône étoile est délégué au listener (typiquement le
 *         {@link com.example.snaptaplaque.viewmodels.SharedViewModel#toggleFavorite(Vehicle)}).
 *         Ce mode garantit la synchronisation des données entre tous les fragments.</li>
 *     <li><strong>Sans listener :</strong> lorsqu'aucun listener n'est fourni
 *         (constructeur {@link #VehicleAdapter(List)}), le clic sur l'icône étoile
 *         modifie directement l'état favori du véhicule de manière locale.</li>
 * </ol>
 * </p>
 *
 * <p>La liste affichée peut être intégralement remplacée à tout moment via la méthode
 * {@link #updateList(List)}, qui déclenche un rafraîchissement complet du
 * {@link RecyclerView} via {@link #notifyDataSetChanged()}.</p>
 *
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * VehicleAdapter adapter = new VehicleAdapter(
 *     new ArrayList<>(),
 *     vehicle -> sharedViewModel.toggleFavorite(vehicle)
 * );
 * recyclerView.setAdapter(adapter);
 *
 * sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
 *     adapter.updateList(vehicles);
 * });
 * }</pre>
 *
 * @see RecyclerView.Adapter
 * @see VehicleViewHolder
 * @see Vehicle
 * @see OnFavoriteClickListener
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 */
public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    /**
     * La liste des véhicules à afficher dans le {@link RecyclerView}.
     *
     * <p>Cette liste peut être remplacée à tout moment via {@link #updateList(List)}.
     * Elle ne doit jamais être {@code null}.</p>
     */
    private List<Vehicle> vehicleList;

    private OnVehicleClickListener vehicleClickListener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    /**
     * Listener optionnel notifié lorsque l'utilisateur clique sur l'icône favori
     * d'un véhicule.
     *
     * <p>Si {@code null}, le clic sur l'icône étoile modifie directement l'état
     * favori du {@link Vehicle} de manière locale, sans notification externe.</p>
     */
    private OnFavoriteClickListener favoriteClickListener;

    /**
     * Interface de callback pour les événements de clic sur l'icône favori d'un véhicule.
     *
     * <p>Permet de déléguer la logique de gestion des favoris à un composant externe
     * (typiquement le {@link com.example.snaptaplaque.viewmodels.SharedViewModel}),
     * assurant ainsi la synchronisation des données entre les différents fragments
     * de l'application.</p>
     *
     * <h3>Exemple d'implémentation :</h3>
     * <pre>{@code
     * OnFavoriteClickListener listener = vehicle -> sharedViewModel.toggleFavorite(vehicle);
     * }</pre>
     *
     * @see com.example.snaptaplaque.viewmodels.SharedViewModel#toggleFavorite(Vehicle)
     */
    public interface OnFavoriteClickListener {
        /**
         * Appelé lorsque l'utilisateur clique sur l'icône favori d'un véhicule.
         *
         * @param vehicle le {@link Vehicle} dont l'état favori doit être inversé ;
         *                ne sera jamais {@code null}
         */
        void onFavoriteClick(Vehicle vehicle);
    }

    /**
     * Construit un nouvel adaptateur avec la liste de véhicules spécifiée, sans listener de favoris.
     *
     * <p>Dans ce mode, le clic sur l'icône étoile modifie directement l'état favori
     * du véhicule via {@link Vehicle#setFavorite(boolean)} sans notification externe.
     * Ce constructeur est adapté aux cas d'utilisation simples ne nécessitant pas
     * de synchronisation avec un ViewModel partagé.</p>
     *
     * @param vehicleList la liste de {@link Vehicle} à afficher ;
     *                    ne doit pas être {@code null}
     */
    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    /**
     * Construit un nouvel adaptateur avec la liste de véhicules et un listener de favoris.
     *
     * <p>Dans ce mode, chaque clic sur l'icône étoile d'un véhicule déclenche
     * l'appel à {@link OnFavoriteClickListener#onFavoriteClick(Vehicle)}, permettant
     * de déléguer la logique de gestion des favoris au
     * {@link com.example.snaptaplaque.viewmodels.SharedViewModel}. C'est le mode
     * recommandé pour garantir la synchronisation des données entre les fragments
     * {@link com.example.snaptaplaque.fragments.HistoryFragment} et
     * {@link com.example.snaptaplaque.fragments.ProfileFragment}.</p>
     *
     * @param vehicleList la liste de {@link Vehicle} à afficher ;
     *                    ne doit pas être {@code null}
     * @param vehicleListener    le {@link OnVehicleClickListener} à notifier lors du clic
     *      *                    sur le bloc du véhicule ; peut être {@code null} (comportement
     *      *                    identique au constructeur {@link #VehicleAdapter(List)})
     * @param favoriteListener    le {@link OnFavoriteClickListener} à notifier lors du clic
     *                    sur l'icône favori ; peut être {@code null} (comportement
     *                    identique au constructeur {@link #VehicleAdapter(List)})
     */
    public VehicleAdapter(List<Vehicle> vehicleList, OnVehicleClickListener vehicleListener, OnFavoriteClickListener favoriteListener) {
        this.vehicleList = vehicleList;
        this.vehicleClickListener = vehicleListener;
        this.favoriteClickListener = favoriteListener;
    }

    /**
     * Remplace intégralement la liste des véhicules affichés et rafraîchit le {@link RecyclerView}.
     *
     * <p>Cette méthode est typiquement appelée depuis un observateur {@link androidx.lifecycle.LiveData}
     * pour mettre à jour l'affichage lorsque les données du
     * {@link com.example.snaptaplaque.viewmodels.SharedViewModel} changent :</p>
     * <pre>{@code
     * sharedViewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
     *     adapter.updateList(vehicles);
     * });
     * }</pre>
     *
     * <p><strong>Note :</strong> cette méthode appelle {@link #notifyDataSetChanged()},
     * ce qui provoque un rafraîchissement complet de tous les éléments visibles.
     * </p>
     *
     * @param newList la nouvelle liste de {@link Vehicle} à afficher ;
     *                ne doit pas être {@code null}
     */
    public void updateList(List<Vehicle> newList) {
        this.vehicleList = newList;
        notifyDataSetChanged();
    }


    /**
     * Crée un nouveau {@link VehicleViewHolder} en gonflant le layout {@code item_vehicle}.
     *
     * <p>Appelé par le {@link RecyclerView} lorsqu'un nouveau ViewHolder est nécessaire
     * pour représenter un élément de la liste. Le layout {@code item_vehicle.xml} est
     * gonflé via le {@link LayoutInflater} du contexte parent.</p>
     *
     * @param parent   le {@link ViewGroup} parent dans lequel la nouvelle vue sera ajoutée
     *                 après avoir été liée à une position d'adaptateur
     * @param viewType le type de vue du nouvel élément (non utilisé ici car l'adaptateur
     *                 ne gère qu'un seul type de vue)
     * @return une nouvelle instance de {@link VehicleViewHolder} contenant la vue gonflée
     * à partir de {@code item_vehicle.xml}
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
     *
     * <p>Cette méthode effectue les opérations suivantes :
     * <ol>
     *     <li>Affecte le numéro d'immatriculation au {@link TextView} correspondant</li>
     *     <li>Affecte les détails du véhicule au {@link TextView} correspondant</li>
     *     <li>Met à jour l'icône de favori en fonction de l'état actuel du véhicule
     *         ({@code ic_star} si favori, {@code ic_star_outline} sinon)</li>
     *     <li>Configure le listener de clic sur l'icône favori :
     *         <ul>
     *             <li>Si un {@link OnFavoriteClickListener} est défini, le clic lui est délégué</li>
     *             <li>Sinon, l'état favori est inversé directement sur l'objet {@link Vehicle}</li>
     *         </ul>
     *     </li>
     *     <li>Met à jour immédiatement l'icône étoile après le clic pour un retour
     *         visuel instantané</li>
     * </ol>
     * </p>
     *
     * @param holder   le {@link VehicleViewHolder} à mettre à jour avec les données du véhicule
     * @param position la position de l'élément dans la liste {@link #vehicleList}
     *                 (index basé sur 0)
     */
    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);

        holder.tvImmatriculation.setText(vehicle.getImmatriculation());
        holder.tvDetails.setText(vehicle.getDetails());

        holder.ivFavorite.setImageResource(
                vehicle.isFavorite() ? R.drawable.ic_star : R.drawable.ic_star_outline
        );

        holder.itemView.setOnClickListener(v -> {
            if(vehicleClickListener != null) {
                vehicleClickListener.onVehicleClick(vehicle);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(vehicle);
            } else {
                vehicle.setFavorite(!vehicle.isFavorite());
            }
            holder.ivFavorite.setImageResource(
                    vehicle.isFavorite() ? R.drawable.ic_star : R.drawable.ic_star_outline
            );
        });
    }

    /**
     * Retourne le nombre total de véhicules dans la liste.
     *
     * <p>Cette valeur détermine le nombre d'éléments que le {@link RecyclerView}
     * doit afficher. Elle est mise à jour automatiquement après chaque appel
     * à {@link #updateList(List)}.</p>
     *
     * @return le nombre d'éléments dans {@link #vehicleList} ; retourne {@code 0}
     * si la liste est vide
     */
    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    /**
     * ViewHolder interne contenant les références aux vues de chaque élément
     * de la liste de véhicules.
     *
     * <p>Ce ViewHolder met en cache les vues du layout {@code item_vehicle.xml}
     * afin d'éviter des appels répétés à {@link View#findViewById(int)}
     * et ainsi améliorer les performances de défilement du {@link RecyclerView}.
     * Les vues mises en cache sont :
     * <ul>
     *     <li>{@link #tvImmatriculation} — numéro d'immatriculation</li>
     *     <li>{@link #tvDetails} — détails descriptifs du véhicule</li>
     *     <li>{@link #ivFavorite} — icône de statut favori (étoile pleine ou vide)</li>
     *     <li>{@link #ivCar} — image représentative du véhicule</li>
     * </ul>
     * </p>
     *
     * @see RecyclerView.ViewHolder
     */
    public static class VehicleViewHolder extends RecyclerView.ViewHolder {

        /**
         * Le {@link TextView} affichant le numéro d'immatriculation du véhicule.
         *
         * <p>Correspond à la vue {@code R.id.tvImmatriculation} dans le layout
         * {@code item_vehicle.xml}.</p>
         */
        TextView tvImmatriculation;

        /**
         * Le {@link TextView} affichant les détails descriptifs du véhicule
         * (marque, modèle, motorisation, etc.).
         *
         * <p>Correspond à la vue {@code R.id.tvDetails} dans le layout
         * {@code item_vehicle.xml}.</p>
         */
        TextView tvDetails;

        /**
         * L'{@link ImageView} affichant l'icône de statut favori.
         *
         * <p>Affiche {@code R.drawable.ic_star} lorsque le véhicule est marqué
         * comme favori, ou {@code R.drawable.ic_star_outline} dans le cas contraire.
         * Un listener de clic est configuré dans
         * {@link VehicleAdapter#onBindViewHolder(VehicleViewHolder, int)} pour
         * permettre l'inversion de l'état favori.</p>
         *
         * <p>Correspond à la vue {@code R.id.ivFavorite} dans le layout
         * {@code item_vehicle.xml}.</p>
         */
        ImageView ivFavorite;

        /**
         * L'{@link ImageView} affichant l'image représentative du véhicule.
         *
         * <p>Correspond à la vue {@code R.id.ivVehicle} dans le layout
         * {@code item_vehicle.xml}.</p>
         */
        ImageView ivCar;

        /**
         * Construit un nouveau {@code VehicleViewHolder} et récupère les références
         * aux vues enfants du layout {@code item_vehicle.xml}.
         *
         * <p>Les vues suivantes sont récupérées et mises en cache :
         * <ul>
         *     <li>{@code R.id.tvImmatriculation} → {@link #tvImmatriculation}</li>
         *     <li>{@code R.id.tvDetails} → {@link #tvDetails}</li>
         *     <li>{@code R.id.ivFavorite} → {@link #ivFavorite}</li>
         *     <li>{@code R.id.ivVehicle} → {@link #ivCar}</li>
         * </ul>
         * </p>
         *
         * @param itemView la vue racine de l'élément de la liste, gonflée à partir
         *                 de {@code item_vehicle.xml} ; ne doit pas être {@code null}
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