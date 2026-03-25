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
 * Classe utilitaire pour gérer la sélection et la capture de photos.
 */
public class Photo {

    private final Context context;
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final ActivityResultLauncher<Uri> cameraLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;
    private Uri tempImageUri;

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
     * Affiche une boîte de dialogue permettant de choisir entre la caméra et la galerie.
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
     * Vérifie les permissions pour l'utilisation de la caméra.
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
     * Prépare l'URI temporaire et lance la caméra.
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
     * Retourne l'URI de l'image temporaire capturée par la caméra.
     *
     * @return Uri de l'image
     */
    public Uri getTempImageUri() {
        return tempImageUri;
    }

    public void setTempImageUri(Uri uri) {
        this.tempImageUri = uri;
    }
}
