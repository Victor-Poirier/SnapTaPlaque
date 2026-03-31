package com.example.snaptaplaque.network.apicall;

import com.example.snaptaplaque.models.api.favorites.FavoriteAllResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesAddResponse;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveRequest;
import com.example.snaptaplaque.models.api.favorites.FavoritesRemoveResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestionnaire d'appels API pour la gestion des véhicules favoris.
 * <p>Cette classe fournit les méthodes nécessaires pour ajouter, supprimer et
 * lister les véhicules marqués comme favoris par l'utilisateur connecté.</p>
 */
public class FavoritesCall {

    /** Instance du service API générée par Retrofit. */
    private static ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
    /**
     * Récupération du gestionnaire de session partagé.
     * Note : Utilise l'instance statique déjà initialisée dans AccountCall.
     */
    private static SessionManager sessionManager = AccountCall.sessionManager;

    /**
     * Ajoute un véhicule à la liste des favoris de l'utilisateur.
     *
     * @param favoritesAddRequest Objet contenant la plaque d'immatriculation à ajouter.
     * @param apiCallback        Callback pour notifier le succès ou l'échec de l'ajout.
     */
    public static void addFavorite(FavoritesAddRequest favoritesAddRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.add("Bearer " + token, favoritesAddRequest.getLicensePlate())
                .enqueue(new Callback<FavoritesAddResponse>() {
                    /**
                     * Invoquée lorsque le serveur confirme (ou non) l'ajout en favori.
                     * @param call     L'appel réseau.
                     * @param response La réponse HTTP contenant la confirmation.
                     */
                    @Override
                    public void onResponse(Call<FavoritesAddResponse> call, Response<FavoritesAddResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur de communication lors de l'ajout.
                     * @param call L'appel réseau interrompu.
                     * @param t    L'exception réseau rencontrée.
                     */
                    @Override
                    public void onFailure(Call<FavoritesAddResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Supprime un véhicule de la liste des favoris de l'utilisateur.
     *
     * @param favoritesRemoveRequest Objet contenant la plaque d'immatriculation à retirer.
     * @param apiCallback           Callback pour notifier la vue du changement.
     */
    public static void removeFavorite(FavoritesRemoveRequest favoritesRemoveRequest, ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.remove("Bearer " + token, favoritesRemoveRequest.getLicensePlate())
                .enqueue(new Callback<FavoritesRemoveResponse>() {
                    /**
                     * Invoquée lors du retour de la demande de suppression.
                     * @param call     L'appel réseau.
                     * @param response La réponse HTTP.
                     */
                    @Override
                    public void onResponse(Call<FavoritesRemoveResponse> call, Response<FavoritesRemoveResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur technique lors de la suppression.
                     */
                    @Override
                    public void onFailure(Call<FavoritesRemoveResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }

    /**
     * Récupère l'intégralité des véhicules favoris de l'utilisateur.
     * <p>Cette méthode est essentielle pour synchroniser l'affichage au démarrage
     * ou lors de l'ouverture de l'onglet Favoris.</p>
     *
     * @param apiCallback Callback retournant la liste complète dans {@link FavoriteAllResponse}.
     */
    public static void allFavorites(ApiCallback apiCallback){
        String token = sessionManager.getToken();
        apiService.all("Bearer " + token)
                .enqueue(new Callback<FavoriteAllResponse>() {
                    /**
                     * Invoquée lors de la réception de la liste des favoris.
                     * @param call     L'appel réseau.
                     * @param response La réponse contenant la liste des véhicules favoris.
                     */
                    @Override
                    public void onResponse(Call<FavoriteAllResponse> call, Response<FavoriteAllResponse> response) {
                        if (response.isSuccessful() && response.body() != null){
                            apiCallback.onResponseSuccess(response);
                        }
                        else {
                            apiCallback.onResponseFailure(response);
                        }
                    }

                    /**
                     * Invoquée en cas d'erreur de chargement de la liste.
                     */
                    @Override
                    public void onFailure(Call<FavoriteAllResponse> call, Throwable t) {
                        apiCallback.onCallFailure(t);
                    }
                });
    }
}
