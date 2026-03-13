package com.example.snaptaplaque.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.Profil;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.vehicles.HistoryVehiclesResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.FavoritesCall;
import com.example.snaptaplaque.network.apicall.VehiclesCall;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Response;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<List<Vehicle>> vehicleList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Vehicle>> favoriteList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private Profil currentProfil;

    // Cache pour éviter les appels redondants
    private boolean historyLoaded = false;
    private boolean favoritesLoaded = false;

    // Méthode unique pour charger les données
    public void loadDataIfNeeded() {
        if (!historyLoaded) {
            loadHistoryFromAPI();
        } else if (!favoritesLoaded) {
            loadFavoritesFromAPI();
        }
    }

    // Force le rechargement des données
    public void forceReload() {
        historyLoaded = false;
        favoritesLoaded = false;
        vehicleList.setValue(new ArrayList<>());
        loadDataIfNeeded();
    }

    private void loadHistoryFromAPI() {
        VehiclesCall.vehiclesHistory(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                HistoryVehiclesResponse history = (HistoryVehiclesResponse) response.body();
                List<InfoResponse> list = history.getHistory();

                List<Vehicle> vehicles = new ArrayList<>();
                for(InfoResponse v : list) {
                    vehicles.add(v.createVehicles(false)); // Par défaut pas favoris
                }

                vehicleList.setValue(vehicles);
                historyLoaded = true;

                // Charger les favoris APRÈS l'historique
                loadFavoritesFromAPI();
            }

            @Override
            public void onResponseFailure(Response response) {
                Log.e("History", "Error: " + response.message());
                historyLoaded = true; // Marquer comme "tenté" même en cas d'erreur
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e("History", "Failure: " + t.getMessage());
                historyLoaded = true;
            }
        });
    }

    private void loadFavoritesFromAPI() {
        if (!historyLoaded) return; // Attendre que l'historique soit chargé

        FavoritesCall.allFavorites(new ApiCallback() {
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

            @Override
            public void onResponseFailure(Response response) {
                Log.e("SyncFavorites", "Error: " + response.message());
                favoritesLoaded = true;
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e("SyncFavorites", "Failure: " + t.getMessage());
                favoritesLoaded = true;
            }
        });
    }

    public void setProfil(Profil currentProfil) {
        this.currentProfil = currentProfil;
    }

    public Profil getCurrentProfil() {
        return currentProfil;
    }

    public LiveData<List<Vehicle>> getVehicleList() {
        return vehicleList;
    }

    public LiveData<List<Vehicle>> getFavoriteList() {
        return favoriteList;
    }

    public void addVehicle(Vehicle vehicle) {
        List<Vehicle> current = vehicleList.getValue();
        if (current != null) {
            // Vérifier si le véhicule existe déjà
            boolean exists = current.stream()
                    .anyMatch(v -> v.getImmatriculation().equals(vehicle.getImmatriculation()));

            if (!exists) {
                current.add(vehicle);
                vehicleList.setValue(current);
                refreshFavoriteList();
            }
        }
    }

    public void setVehicles(List<Vehicle> vehicles) {
        vehicleList.setValue(vehicles);
        refreshFavoriteList();
    }

    public void toggleFavorite(Vehicle vehicle) {
        vehicle.setFavorite(!vehicle.isFavorite());
        vehicleList.setValue(vehicleList.getValue());
        refreshFavoriteList();
    }

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

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

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
