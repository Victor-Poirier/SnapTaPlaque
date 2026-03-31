package com.example.snaptaplaque.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client de configuration centralisé pour les communications réseau via Retrofit.
 * <p>Cette classe implémente le pattern <b>Singleton</b> pour garantir qu'une seule instance
 * de l'objet {@link Retrofit} est créée et utilisée tout au long du cycle de vie
 * de l'application SnapTaPlaque.</p>
 * <p>Elle configure l'URL de base du serveur et définit le convertisseur JSON (GSON)
 * utilisé pour transformer les réponses HTTP en objets Java (DTO).</p>
 */
public class ApiClient {

    /**
     * URL de base du serveur API.
     * <p>Note : Actuellement configurée sur un tunnel ngrok pour le développement.
     * Une alternative locale (10.0.2.2 pour l'émulateur Android) est disponible en commentaire.</p>
     */
    private static final String BASE_URL = "https://danny-nonpresumptive-jadedly.ngrok-free.dev";
    //private static final String BASE_URL = "http://10.0.2.2:8000/";

    /** Instance unique de Retrofit partagée par l'application. */
    private static Retrofit retrofit;

    /**
     * Fournit l'instance unique de {@link Retrofit}.
     * * <p>Si l'instance n'existe pas encore (premier appel), elle est initialisée
     * avec la configuration suivante :</p>
     * <ul>
     * <li>L'URL de base définie dans {@link #BASE_URL}.</li>
     * <li>Un convertisseur {@link GsonConverterFactory} pour le parsing automatique du JSON.</li>
     * </ul>
     * * @return L'instance configurée de {@link Retrofit} prête à créer des services API.
     */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
