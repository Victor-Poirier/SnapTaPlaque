package com.example.snaptaplaque.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.snaptaplaque.models.Profil;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.FavoritesCall;
import com.example.snaptaplaque.network.apicall.VehiclesCall;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Response;

/**
 * ViewModel partagé gérant l'état global des données de l'application.
 * <p>Cette classe centralise la liste des véhicules (historique), les favoris,
 * le profil utilisateur et les requêtes de recherche. Elle permet une persistance
 * des données lors des changements de configuration (rotation d'écran).</p>
 */
public class SharedViewModel extends ViewModel {

    /** Liste observable de tous les véhicules détectés ou consultés. */
    private final MutableLiveData<List<Vehicle>> vehicleList = new MutableLiveData<>(new ArrayList<>());
    /** Liste observable des véhicules marqués comme favoris. */
    private final MutableLiveData<List<Vehicle>> favoriteList = new MutableLiveData<>(new ArrayList<>());
    /** Requête de recherche actuelle pour le filtrage en temps réel. */
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    /** Objet représentant le profil de l'utilisateur connecté. */
    private Profil currentProfil;

    /** Indicateur de chargement de l'historique pour éviter les appels API redondants. */
    private boolean historyLoaded = false;
    /** Indicateur de chargement des favoris. */
    private boolean favoritesLoaded = false;

    /**
     * Déclenche le chargement initial des données si nécessaire.
     * <p>Respecte un ordre séquentiel : charge d'abord l'historique,
     * puis les favoris une fois l'historique disponible.</p>
     */
    public void loadDataIfNeeded() {
        if (!historyLoaded) {
            loadHistoryFromAPI();
        } else if (!favoritesLoaded) {
            loadFavoritesFromAPI();
        }
    }

    /**
     * Réinitialise les caches et force un nouveau téléchargement des données depuis le serveur.
     */
    public void forceReload() {
        historyLoaded = false;
        favoritesLoaded = false;
        vehicleList.setValue(new ArrayList<>());
        loadDataIfNeeded();
    }

    /**
     * Appelle l'API pour récupérer l'historique des véhicules.
     * <p>En cas de succès, transforme les {@link InfoResponse} en objets métier {@link Vehicle}
     * et enchaîne sur le chargement des favoris.</p>
     */
    private void loadHistoryFromAPI() {
        VehiclesCall.vehiclesHistory(new ApiCallback() {
            /**
             * Invoquée lorsque le serveur renvoie la liste complète de l'historique.
             * @param response La réponse contenant l'objet {@link HistoryVehiclesResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                HistoryVehiclesResponse history = (HistoryVehiclesResponse) response.body();
                List<InfoResponse> list = history.getHistory();

                List<Vehicle> vehicles = new ArrayList<>();
                for(InfoResponse v : list) {
                    vehicles.add(0, v.createVehicles(false)); // Par défaut pas favoris
                }

                vehicleList.setValue(vehicles);
                historyLoaded = true;

                // Charger les favoris APRÈS l'historique
                loadFavoritesFromAPI();
            }

            /**
             * Invoquée si le serveur répond par un code d'erreur (ex: 500, 404).
             * @param response La réponse HTTP contenant les détails de l'erreur.
             */
            @Override
            public void onResponseFailure(Response response) {
                Log.e("History", "Error: " + response.message());
                historyLoaded = true; // Marquer comme "tenté" même en cas d'erreur
            }

            /**
             * Invoquée en cas de rupture de connexion ou de timeout réseau.
             * @param t L'exception technique rencontrée.
             */
            @Override
            public void onCallFailure(Throwable t) {
                Log.e("History", "Failure: " + t.getMessage());
                historyLoaded = true;
            }
        });
    }

    /**
     * Appelle l'API pour récupérer les favoris et synchronise l'état avec la liste globale.
     * <p>Si un véhicule favori existe déjà dans l'historique, il est marqué comme tel.
     * Sinon, il est ajouté à la liste globale.</p>
     */
    private void loadFavoritesFromAPI() {
        if (!historyLoaded) return; // Attendre que l'historique soit chargé

        FavoritesCall.allFavorites(new ApiCallback() {
            /**
             * Invoquée lorsque la liste des favoris est récupérée avec succès.
             * @param response La réponse contenant la liste {@link FavoriteAllResponse}.
             */
            @Override
            public void onResponseSuccess(Response response) {
                FavoriteAllResponse res = (FavoriteAllResponse) response.body();
                List<InfoResponse> favoriteResponses = res.getFavorites();

                List<Vehicle> allVehicles = vehicleList.getValue();
                if (allVehicles != null) {
                    // Marquer les favoris
                    for(InfoResponse infoResponse : favoriteResponses) {
                        String licensePlate = infoResponse.getLicensePlate();

                        Vehicle existingVehicle = allVehicles.stream()
                                .filter(v -> v.getImmatriculation().equals(licensePlate))
                                .findFirst()
                                .orElse(null);

                        if (existingVehicle != null) {
                            existingVehicle.setFavorite(true);
                        } else {
                            Vehicle newVehicle = infoResponse.createVehicles(true);
                            allVehicles.add(newVehicle);
                        }
                    }

                    vehicleList.setValue(allVehicles);
                    favoritesLoaded = true;
                    refreshFavoriteList();
                }
            }

            /**
             * Invoquée si le serveur renvoie une erreur (ex: Token expiré).
             */
            @Override
            public void onResponseFailure(Response response) {
                Log.e("SyncFavorites", "Error: " + response.message());
                favoritesLoaded = true;
            }

            /**
             * Invoquée en cas d'erreur réseau critique.
             */
            @Override
            public void onCallFailure(Throwable t) {
                Log.e("SyncFavorites", "Failure: " + t.getMessage());
                favoritesLoaded = true;
            }
        });
    }

    /**
     * Définit le profil de l'utilisateur actuellement connecté au ViewModel.
     * @param currentProfil L'objet {@link Profil} contenant les informations de l'utilisateur.
     */
    public void setProfil(Profil currentProfil) {
        this.currentProfil = currentProfil;
    }

    /**
     * Récupère le profil de l'utilisateur chargé dans la session actuelle.
     * @return L'objet {@link Profil} de l'utilisateur, ou {@code null} s'il n'est pas défini.
     */
    public Profil getCurrentProfil() {
        return currentProfil;
    }

    /**
     * Fournit un accès en lecture seule à la liste complète des véhicules (historique).
     * <p>Les fragments peuvent observer ce {@link LiveData} pour mettre à jour l'interface
     * dès que l'historique est chargé ou modifié.</p>
     * @return Un {@link LiveData} contenant la liste des véhicules.
     */
    public LiveData<List<Vehicle>> getVehicleList() {
        return vehicleList;
    }

    /**
     * Fournit un accès en lecture seule à la liste filtrée des favoris.
     * <p>Ce {@link LiveData} est mis à jour automatiquement via la méthode interne
     * {@code refreshFavoriteList()}.</p>
     * @return Un {@link LiveData} contenant uniquement les véhicules marqués en favoris.
     */
    public LiveData<List<Vehicle>> getFavoriteList() {
        return favoriteList;
    }

    /**
     * Ajoute un nouveau véhicule à la liste globale s'il n'existe pas déjà.
     * @param vehicle Le véhicule à ajouter.
     */
    public void addVehicle(Vehicle vehicle) {
        List<Vehicle> current = vehicleList.getValue();
        if (current != null) {
            // Vérifier si le véhicule existe déjà
            boolean exists = current.stream()
                    .anyMatch(v -> v.getImmatriculation().equals(vehicle.getImmatriculation()));

            if (!exists) {
                current.add(0, vehicle);
                vehicleList.setValue(current);
                refreshFavoriteList();
            }
        }
    }

    /**
     * Met à jour massivement la liste des véhicules.
     */
    public void setVehicles(List<Vehicle> vehicles) {
        vehicleList.setValue(vehicles);
        refreshFavoriteList();
    }

    /**
     * Alterne l'état favori d'un véhicule et met à jour les listes observées.
     * @param vehicle Le véhicule concerné.
     */
    public void toggleFavorite(Vehicle vehicle) {
        vehicle.setFavorite(!vehicle.isFavorite());
        vehicleList.setValue(vehicleList.getValue());
        refreshFavoriteList();
    }

    /**
     * Filtre la liste globale pour extraire uniquement les favoris
     * et met à jour le profil utilisateur.
     */
    private void refreshFavoriteList() {
        List<Vehicle> all = vehicleList.getValue();
        if (all != null) {
            List<Vehicle> favorites = all.stream()
                    .filter(Vehicle::isFavorite)
                    .collect(Collectors.toList());
            favoriteList.setValue(favorites);

            if (currentProfil != null) {
                currentProfil.setFavoriteVehicule(favorites);
            }
        }
    }

    /**
     * Définit la chaîne de caractères utilisée pour le filtrage de la liste.
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    /**
     * Retourne une liste de véhicules filtrée selon la recherche actuelle.
     * <p>Recherche des correspondances dans l'immatriculation, la marque,
     * le modèle, l'énergie et les informations complémentaires.</p>
     * @return Une sous-liste de véhicules correspondant aux critères.
     */
    public List<Vehicle> getFilteredVehicles() {
        String query = searchQuery.getValue().toLowerCase();
        List<Vehicle> allVehicles = vehicleList.getValue();

        if (allVehicles == null) return new ArrayList<>();
        if (query.isEmpty()) return allVehicles;

        return allVehicles.stream()
                .filter(vehicle -> vehicle.getImmatriculation().toLowerCase().contains(query) ||
                        vehicle.getBrand().toLowerCase().contains(query) ||
                        vehicle.getModel().toLowerCase().contains(query) ||
                        vehicle.getInfo().toLowerCase().contains(query) ||
                        vehicle.getEnergy().toLowerCase().contains(query)
                )
                .collect(Collectors.toList());
    }
}
