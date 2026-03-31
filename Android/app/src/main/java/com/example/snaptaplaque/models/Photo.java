package com.example.snaptaplaque.models;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.snaptaplaque.R;

import java.io.File;

/**
 * Classe utilitaire pour orchestrer la capture et la sélection de photos dans l'application.
 * * <p>Cette classe centralise la gestion des flux d'images :</p>
 * <ul>
 * <li>Affichage du choix de la source (Caméra ou Galerie).</li>
 * <li>Vérification et demande dynamique des permissions (Caméra).</li>
 * <li>Génération d'URIs sécurisées via un {@link FileProvider} pour la capture photo.</li>
 * <li>Déclenchement des {@link ActivityResultLauncher} pour obtenir le résultat final.</li>
 * </ul>
 */
public class Photo {

    /** Contexte de l'application. */
    private final Context context;
    /** Launcher pour la demande de permission de la caméra. */
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    /** Launcher pour la capture de photo. */
    private final ActivityResultLauncher<Uri> cameraLauncher;
    /** Launcher pour la sélection d'image depuis la galerie. */
    private final ActivityResultLauncher<String> galleryLauncher;
    /** URI temporaire pour l'image capturée ou sélectionnée. */
    private Uri tempImageUri;

    /**
     * Constructeur de la classe Photo.
     *
     * @param context                   Le contexte de l'activité ou du fragment.
     * @param requestPermissionLauncher Le launcher pour demander la permission CAMERA.
     * @param cameraLauncher            Le launcher pour démarrer l'application appareil photo.
     * @param galleryLauncher           Le launcher pour ouvrir le sélecteur de fichiers média.
     */
    public Photo(Context context,
                 ActivityResultLauncher<String> requestPermissionLauncher,
                 ActivityResultLauncher<Uri> cameraLauncher,
                 ActivityResultLauncher<String> galleryLauncher) {
        this.context = context;
        this.requestPermissionLauncher = requestPermissionLauncher;
        this.cameraLauncher = cameraLauncher;
        this.galleryLauncher = galleryLauncher;
    }


    /**
     * Affiche une boîte de dialogue modale permettant à l'utilisateur de choisir la source de l'image.
     * * <p>L'utilisateur peut choisir entre :</p>
     * 1. Prendre une photo (nécessite vérification de permission).
     * 2. Choisir une image existante dans la galerie (via un sélecteur de type MIME image/*).
     */
    public void showChoice() {
        String[] options = {
                context.getString(R.string.camera_choice),
                context.getString(R.string.gallery_choice)
        };

        new AlertDialog.Builder(context)
                .setTitle(R.string.edit_photo)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                }).show();
    }

    /**
     * Vérifie si l'application possède la permission CAMERA.
     * * <p>Si la permission est déjà accordée, lance l'appareil photo via {@link #openCamera()}.
     * Sinon, déclenche le launcher de demande de permission.</p>
     */
    public void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    /**
     * Prépare le stockage temporaire pour la photo et lance l'intent de capture.
     * * <p>Cette méthode crée un fichier temporaire dans le répertoire des images de l'application,
     * génère une URI sécurisée via le {@link FileProvider} pour permettre à l'application
     * caméra d'écrire dedans, puis lance la capture.</p>
     */
    public void openCamera() {
        File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image.jpg");

        tempImageUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                tempFile
        );

        cameraLauncher.launch(tempImageUri);
    }

    /**
     * Retourne l'URI du fichier image temporaire utilisé lors de la capture caméra.
     *
     * @return L'{@link Uri} pointant vers l'emplacement de l'image capturée.
     */
    public Uri getTempImageUri() {
        return tempImageUri;
    }

    /**
     * Définit manuellement l'URI de l'image temporaire (utile lors d'une restauration d'état).
     *
     * @param uri La nouvelle {@link Uri} à enregistrer.
     */
    public void setTempImageUri(Uri uri) {
        this.tempImageUri = uri;
    }
}
