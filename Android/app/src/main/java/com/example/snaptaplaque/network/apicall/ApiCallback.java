package com.example.snaptaplaque.network.apicall;

import retrofit2.Response;

/**
 * Interface de rappel (Callback) générique pour la gestion des retours d'appels API.
 * <p>Cette interface définit un contrat standard pour traiter les trois issues possibles
 * d'une requête réseau asynchrone dans l'application SnapTaPlaque. Elle permet de
 * centraliser la logique de réponse tout en restant flexible sur le type de données traitées.</p>
 * <p>L'utilisation de cette interface facilite le découplage entre les couches réseau
 * (classes {@code Call}) et la couche présentation (Fragments/Activities).</p>
 */
public interface ApiCallback {

    /**
     * Invoquée lorsque le serveur renvoie une réponse considérée comme un succès.
     * * <p>Cette méthode est appelée si le code de statut HTTP est compris entre 200 et 299.
     * Le traitement des données (parsing du {@code response.body()}) doit être effectué
     * à l'intérieur de cette implémentation.</p>
     *
     * @param response L'objet {@link Response} de Retrofit contenant les données typées
     * attendues par l'appelant.
     */
    void onResponseSuccess(Response response);

    /**
     * Invoquée lorsque le serveur renvoie une réponse, mais que celle-ci indique une erreur.
     * * <p>Cette méthode traite les erreurs logiques envoyées par le backend (ex: 400 Bad Request,
     * 401 Unauthorized, 404 Not Found ou 500 Internal Server Error). Elle permet de
     * récupérer le message d'erreur ou le code d'état pour informer l'utilisateur.</p>
     *
     * @param response L'objet {@link Response} contenant les informations d'erreur
     * fournies par le serveur.
     */
    void onResponseFailure(Response response);

    /**
     * Invoquée en cas d'échec critique lors de l'établissement de la communication.
     * * <p>Cette méthode est déclenchée pour des erreurs de bas niveau où aucune réponse
     * n'a pu être obtenue du serveur (ex: absence de connexion internet, timeout,
     * erreur de résolution DNS ou crash lors du parsing JSON).</p>
     *
     * @param t L'objet {@link Throwable} décrivant la cause technique de l'échec.
     */
    void onCallFailure(Throwable t);
}
