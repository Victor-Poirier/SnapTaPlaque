package com.example.snaptaplaque.models.api;

import android.net.Uri;
import android.widget.ImageView;

/**
 * Classe représentant une requête de prédiction pour l'API de reconnaissance de plaques d'immatriculation.
 * Cette classe encapsule les données nécessaires pour effectuer une prédiction, notamment l'image à analyser.
 * L'image est représentée par un objet {@link Uri} qui pointe vers la source de l'image (par exemple, une image sélectionnée depuis la galerie ou capturée par la caméra).
 */
public class PredictionRequest {

    /**
     * URI de l'image à analyser pour la prédiction.
     * Ce champ est essentiel pour que l'API puisse accéder à l'image et effectuer la reconnaissance de la plaque d'immatriculation.
     */
    private Uri imageUri;

    /**
     * Constructeur de la classe PredictionRequest.
     *
     * @param imageUri URI de l'image à analyser pour la prédiction.
     */
    public PredictionRequest(Uri imageUri) {
        this.imageUri = imageUri;
    }

}
