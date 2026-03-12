package com.example.snaptaplaque.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.snaptaplaque.models.Profil;
import com.example.snaptaplaque.models.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel partagé centralisant les données véhicules et profil de l'application SnapTaPlaque.
 *
 * <p>Ce ViewModel est scopé au niveau de l'activité hôte ({@link com.example.snaptaplaque.activities.MainActivity})
 * et sert de source de vérité unique pour les trois fragments principaux :
 * <ul>
 *     <li>{@link com.example.snaptaplaque.fragments.HistoryFragment} — observe {@link #getVehicleList()}
 *         pour afficher l'historique complet des véhicules scannés</li>
 *     <li>{@link com.example.snaptaplaque.fragments.SearchFragment} — appelle {@link #addVehicle(Vehicle)}
 *         pour ajouter un nouveau véhicule scanné à l'historique</li>
 *     <li>{@link com.example.snaptaplaque.fragments.ProfileFragment} — observe {@link #getFavoriteList()}
 *         pour afficher uniquement les véhicules marqués comme favoris</li>
 * </ul>
 * </p>
 *
 * <p>Le ViewModel expose deux flux {@link LiveData} :
 * <ul>
 *     <li>{@link #vehicleList} — liste complète de tous les véhicules scannés (historique)</li>
 *     <li>{@link #favoriteList} — sous-ensemble filtré contenant uniquement les véhicules
 *         dont {@link Vehicle#isFavorite()} vaut {@code true}</li>
 * </ul>
 * La liste des favoris est automatiquement recalculée à chaque modification
 * (ajout de véhicule, changement d'état favori, remplacement de la liste) via
 * la méthode interne {@link #refreshFavoriteList()}.</p>
 *
 * <p>Le {@link Profil} de l'utilisateur courant est également stocké dans ce ViewModel.
 * Sa liste {@link Profil#favoriteVehicule} est synchronisée avec {@link #favoriteList}
 * à chaque rafraîchissement.</p>
 *
 * <h3>Exemple d'utilisation depuis un fragment :</h3>
 * <pre>{@code
 * SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
 * vm.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
 *     adapter.updateList(vehicles);
 * });
 * vm.toggleFavorite(vehicle);
 * }</pre>
 *
 * @see Vehicle
 * @see Profil
 * @see com.example.snaptaplaque.activities.MainActivity
 */
public class SharedViewModel extends ViewModel {

    /**
     * Liste observable de tous les véhicules scannés (historique complet).
     *
     * <p>Initialisée avec une {@link ArrayList} vide. Chaque modification déclenche
     * une notification aux observateurs inscrits via {@link LiveData#observe}.</p>
     */
    private final MutableLiveData<List<Vehicle>> vehicleList = new MutableLiveData<>(new ArrayList<>());

    /**
     * Liste observable des véhicules marqués comme favoris.
     *
     * <p>Ce flux est un sous-ensemble filtré de {@link #vehicleList}, recalculé
     * automatiquement par {@link #refreshFavoriteList()} à chaque modification
     * de l'historique ou de l'état favori d'un véhicule.</p>
     */
    private final MutableLiveData<List<Vehicle>> favoriteList = new MutableLiveData<>(new ArrayList<>());

    /**
     * Flux observable contenant la requête de recherche courante.
     *
     * <p>Chaque modification déclenche une notification aux observateurs inscrits
     * via {@link LiveData#observe}.</p>
     */
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    /**
     * Profil de l'utilisateur actuellement connecté.
     *
     * <p>Sa liste {@link Profil#favoriteVehicule} est synchronisée avec
     * {@link #favoriteList} à chaque appel à {@link #refreshFavoriteList()}.</p>
     */
    private Profil currentProfil;

    /**
     * Définit le profil de l'utilisateur courant.
     *
     * @param currentProfil le {@link Profil} à associer à ce ViewModel ;
     *                      peut être {@code null} pour dissocier le profil actuel
     */
    public void setProfil(Profil currentProfil) {
        this.currentProfil = currentProfil;
    }

    /**
     * Retourne le profil de l'utilisateur actuellement connecté.
     *
     * @return le {@link Profil} courant, ou {@code null} si aucun profil n'a été défini
     *         via {@link #setProfil(Profil)}
     */
    public Profil getCurrentProfil() {
        return this.currentProfil;
    }

    /**
     * Retourne le flux observable de la liste complète des véhicules (historique).
     *
     * <p>Les fragments doivent observer ce {@link LiveData} pour être notifiés
     * de tout ajout, suppression ou modification d'un véhicule dans l'historique.</p>
     *
     * @return un {@link LiveData} en lecture seule contenant la liste des {@link Vehicle} ;
     *         ne retourne jamais {@code null}
     */
    public LiveData<List<Vehicle>> getVehicleList() {
        return this.vehicleList;
    }

    /**
     * Retourne le flux observable de la liste des véhicules favoris.
     *
     * <p>Cette liste est automatiquement mise à jour lorsque l'état favori d'un
     * véhicule change via {@link #toggleFavorite(Vehicle)} ou lorsque la liste
     * globale est modifiée via {@link #addVehicle(Vehicle)} ou {@link #setVehicles(List)}.</p>
     *
     * @return un {@link LiveData} en lecture seule contenant la liste des {@link Vehicle}
     *         favoris ; ne retourne jamais {@code null}
     */
    public LiveData<List<Vehicle>> getFavoriteList() {
        return this.favoriteList;
    }

    /**
     * Ajoute un véhicule à l'historique s'il n'existe pas déjà.
     *
     * <p>La détection de doublon se fait par comparaison du numéro d'immatriculation
     * ({@link Vehicle#getImmatriculation()}) avec les véhicules déjà présents dans la liste.
     * Si un véhicule portant la même immatriculation existe déjà, l'ajout est ignoré
     * silencieusement.</p>
     *
     * <p>En cas d'ajout réussi, la liste des favoris est automatiquement rafraîchie
     * via {@link #refreshFavoriteList()}.</p>
     *
     * @param vehicle le {@link Vehicle} à ajouter à l'historique ;
     *                ne doit pas être {@code null}
     */
    public void addVehicle(Vehicle vehicle) {
        List<Vehicle> currentList = vehicleList.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        //Évite les doublons par numéro d'immatriculation
        boolean exists = currentList.stream()
                .anyMatch(v -> v.getImmatriculation().equals(vehicle.getImmatriculation()));
        if(!exists){
            currentList.add(vehicle);
            vehicleList.setValue(currentList);
            refreshFavoriteList();
        }

    }

    /**
     * Remplace intégralement la liste des véhicules de l'historique.
     *
     * <p>Cette méthode écrase la liste existante par la nouvelle liste fournie
     * et rafraîchit automatiquement la liste des favoris. Elle est principalement
     * utilisée lors de l'initialisation de l'application avec des données fictives
     * ou lors du chargement de données depuis une source externe.</p>
     *
     * @param vehicles la nouvelle liste de {@link Vehicle} à définir comme historique ;
     *                 peut être {@code null} (sera traité comme une liste vide par les observateurs)
     */
    public void setVehicles(List<Vehicle> vehicles) {
        vehicleList.setValue(vehicles);
        refreshFavoriteList();
    }

    /**
     * Inverse l'état favori d'un véhicule et met à jour les listes observables.
     *
     * <p>Si le véhicule est actuellement favori ({@code isFavorite() == true}),
     * il sera démarqué ; sinon, il sera marqué comme favori. Les deux flux
     * {@link #vehicleList} et {@link #favoriteList} sont immédiatement notifiés
     * du changement afin que tous les fragments observateurs puissent mettre
     * à jour leur interface.</p>
     *
     * <p><strong>Note :</strong> un appel explicite à
     * {@code vehicleList.setValue(vehicleList.getValue())} est effectué pour
     * forcer la notification du {@link LiveData}, car la modification porte
     * sur une propriété interne de l'objet {@link Vehicle} et non sur la
     * référence de la liste elle-même.</p>
     *
     * @param vehicle le {@link Vehicle} dont l'état favori doit être inversé ;
     *                ne doit pas être {@code null}
     */
    public void toggleFavorite(Vehicle vehicle){
        vehicle.setFavorite(!vehicle.isFavorite());
        //Force la notification du LiveDate
        vehicleList.setValue(vehicleList.getValue());
        refreshFavoriteList();
    }

    /**
     * Recalcule la liste des véhicules favoris à partir de l'historique complet.
     *
     * <p>Cette méthode interne filtre {@link #vehicleList} pour ne conserver que
     * les véhicules dont {@link Vehicle#isFavorite()} vaut {@code true}, puis
     * met à jour le flux {@link #favoriteList}. Si un {@link Profil} est défini
     * via {@link #setProfil(Profil)}, sa liste {@link Profil#favoriteVehicule}
     * est également synchronisée.</p>
     *
     * <p>Cette méthode est appelée automatiquement par :
     * <ul>
     *     <li>{@link #addVehicle(Vehicle)} — après l'ajout d'un nouveau véhicule</li>
     *     <li>{@link #setVehicles(List)} — après le remplacement de la liste complète</li>
     *     <li>{@link #toggleFavorite(Vehicle)} — après l'inversion de l'état favori</li>
     * </ul>
     * </p>
     */
    private void refreshFavoriteList(){
        List<Vehicle> all = vehicleList.getValue();
        if(all != null){
            List<Vehicle> favorites = all.stream()
                    .filter(Vehicle::isFavorite)
                    .collect(Collectors.toList());
            favoriteList.setValue(favorites);

            if(currentProfil != null){
                currentProfil.favoriteVehicule.clear();
                currentProfil.favoriteVehicule.addAll(favorites);
            }
        }
    }

    /**
     * Définit la requête de recherche courante.
     *
     * @param query
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    /**
     * Retourne la liste des véhicules qui correspondent à la requête de recherche.
     *
     * @return
     */
    public List<Vehicle> getFilteredVehicles() {
        String query = searchQuery.getValue().toLowerCase();
        List<Vehicle> allVehicles = vehicleList.getValue();

        if (allVehicles == null) return new ArrayList<>();
        if (query.isEmpty()) return allVehicles;

        return allVehicles.stream()
                .filter(v -> v.getImmatriculation().toLowerCase().contains(query)
                        || v.getDetails().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }
}