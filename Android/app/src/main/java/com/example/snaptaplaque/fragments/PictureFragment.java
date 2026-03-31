package com.example.snaptaplaque.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.ml.LicensePlateRecognizer;
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.predictions.PredictionDetectionResult;
import com.example.snaptaplaque.models.api.predictions.PredictionResponse;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.PredictionsCall;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Fragment dédié à la recherche et au scan de plaques d'immatriculation.
 *
 * <p>Ce fragment permet à l'utilisateur de capturer une image via l'appareil photo
 * ou d'en sélectionner une depuis la galerie afin d'identifier un véhicule.
 * Le processus repose sur une analyse locale par reconnaissance optique (OCR)
 * suivie d'un appel API pour récupérer les caractéristiques techniques du véhicule.</p>
 *
 * <p>Le {@link SharedViewModel} assure la synchronisation des données :
 * <ul>
 * <li>Chaque véhicule identifié est ajouté à la liste globale via {@link SharedViewModel#addVehicle(Vehicle)}</li>
 * <li>Les données sont automatiquement reflétées dans le {@link HistoryFragment}</li>
 * </ul>
 * </p>
 *
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 * @see HistoryFragment
 * @see com.example.snaptaplaque.ml.LicensePlateRecognizer
 * @see com.example.snaptaplaque.models.Photo
 */
public class PictureFragment extends Fragment {

    private static final String TAG = "PictureFragment";
    private static final int UPLOAD_MAX_DIMENSION = 1024;
    private static final int UPLOAD_JPEG_QUALITY = 80;

    /** Vue affichant l'image sélectionnée ou capturée. */
    private ImageView ivLicencePlate;
    /** Bouton déclenchant le choix de la source de l'image (Caméra/Galerie). */
    private Button btnPicture;
    /** TextView affichant le résultat de la détection de texte de la plaque. */
    private TextView showPlate;
    /** Bouton lançant l'analyse et la recherche du véhicule. */
    private Button btnSearch;

    /** Utilitaire gérant la capture d'image et les permissions associées. */
    private Photo photo;
    /** ViewModel partagé pour stocker le véhicule détecté dans l'historique. */
    private SharedViewModel sharedViewModel;

    /** Launcher pour la demande de permission système (Caméra). */
    private ActivityResultLauncher<String> requestPermissionLauncher;
    /** Launcher pour capturer une photo et récupérer son URI. */
    private ActivityResultLauncher<Uri> cameraLauncher;
    /** Launcher pour sélectionner un contenu dans la galerie. */
    private ActivityResultLauncher<String> galleryLauncher;

    /** Service d'exécution pour traiter les images lourdement en arrière-plan. */
    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    /** Outil de reconnaissance optique de caractères (OCR) spécialisé pour les plaques. */
    private LicensePlateRecognizer licensePlateRecognizer;

    /**
     * Initialise le fragment et enregistre les contrats d'activité pour les permissions et les médias.
     * * <p>L'initialisation du {@link LicensePlateRecognizer} est lancée de manière asynchrone
     * pour ne pas bloquer le démarrage du fragment.</p>
     *
     * @param savedInstanceState État sauvegardé du fragment (non utilisé ici).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // On enregistre les launchers ICI (obligatoirement dans onCreate ou avant)
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) photo.openCamera();
                    else Toast.makeText(getContext(), R.string.necessary_camera, Toast.LENGTH_SHORT).show();
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if(success && photo.getTempImageUri() != null) {
                        ivLicencePlate.setImageURI(null);
                        ivLicencePlate.setImageURI(photo.getTempImageUri());
                        showUI();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null) {
                        photo.setTempImageUri(uri);
                        ivLicencePlate.setImageURI(null);
                        ivLicencePlate.setImageURI(photo.getTempImageUri());
                        showUI();
                    }
                }
        );

        // Initialisation de l'outil Photo
        photo = new Photo(requireContext(), requestPermissionLauncher, cameraLauncher, galleryLauncher);

        // Initialisation de la reconnaissance de plaque locale (offline)
        licensePlateRecognizer = new LicensePlateRecognizer(requireContext());
        imageExecutor.execute(() -> {
            try {
                licensePlateRecognizer.init();
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize LicensePlateRecognizer", e);
            }
        });
    }

    /**
     * Gonfle le layout et configure les interactions de l'interface utilisateur.
     *
     * @param inflater Le {@link LayoutInflater} pour gonfler la vue.
     * @param container Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue {@link View} racine du fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        ivLicencePlate = view.findViewById(R.id.ivLicencePlate);
        btnPicture = view.findViewById(R.id.btnPicture);
        showPlate = view.findViewById(R.id.showPlate);
        btnSearch = view.findViewById(R.id.btnSearch);

        btnPicture.setOnClickListener(v -> {
            photo.showChoice();
        });

        btnSearch.setOnClickListener(v -> {
            ProgressDialog dialog = ProgressDialog.show(getContext(), "", getString(R.string.research_in_progress), true);

            picturePredict(photo, () -> {
                if((dialog != null) && (dialog.isShowing())) {
                    dialog.dismiss();
                }
            });
        });

        return view;
    }

    /**
     * Active la visibilité des composants UI nécessaires après l'acquisition d'une image.
     */
    private void showUI() {
        ivLicencePlate.setVisibility(View.VISIBLE);
        showPlate.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.VISIBLE);
    }

    /**
     * Orchestre le processus de détection et de recherche à partir d'un objet {@link Photo}.
     * * <p>Cette méthode effectue :
     * <ol>
     * <li>L'optimisation du Bitmap pour éviter les erreurs de mémoire</li>
     * <li>L'analyse OCR via le modèle ML local</li>
     * <li>L'extraction et le nettoyage du texte détecté</li>
     * <li>Le lancement de la requête API en cas de détection valide</li>
     * </ol>
     * </p>
     *
     * @param photo    L'instance contenant l'URI de l'image à traiter.
     * @param callback {@link Runnable} exécuté à la fin du processus (succès ou échec).
     */
    public void picturePredict(Photo photo, Runnable callback) {
        Uri imageUri = photo.getTempImageUri();

        if (imageUri == null) {
            Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
            callback.run();
            return;
        }

        setLoading(true);

        imageExecutor.execute(() -> {
            boolean predictionHandled = false;
            try {
                // Détection de la plaque d'immatriculation dans l'image (local, offline)
                Bitmap bitmap = getOptimizedBitmapFromUri(imageUri);
                if (bitmap != null) {
                    List<LicensePlateRecognizer.PlateResult> results = licensePlateRecognizer.processImage(bitmap);
                    if (!results.isEmpty()) {
                        predictionHandled = true;
                        LicensePlateRecognizer.PlateResult best = results.get(0);
                        runOnMainThread(() -> {
                            setLoading(false);
                            String detectedPlate = extractPlate(best.text);
                            if (detectedPlate != null) {
                                showPlate.setText(detectedPlate);
                                if (plateComplianceVerification(detectedPlate)) {
                                    getInfoVehicle(new InfoRequest(detectedPlate));
                                } else {
                                    Toast.makeText(getContext(), R.string.compliance_plate, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in picturePredict", e);
            }

            if (!predictionHandled) {
                runOnMainThread(() -> {
                    setLoading(false);
                    Toast.makeText(getContext(), R.string.detection_plate, Toast.LENGTH_SHORT).show();
                });
            }
            runOnMainThread(callback);
        });
    }

    /**
     * Valide le format de la plaque d'immatriculation par rapport aux normes SIV françaises.
     *
     * @param plate Le texte de la plaque à vérifier.
     * @return {@code true} si le format est valide, {@code false} sinon.
     */
    private boolean plateComplianceVerification(String plate) {

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS)[A-HJ-NP-TV-Z]{2})";

        if((!plate.matches(regex_1)) && (!plate.matches(regex_2))) {
            Toast.makeText(getContext(), R.string.compliance_plate, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Interroge l'API pour obtenir les détails du véhicule et met à jour le ViewModel.
     * * <p>En cas de succès, un {@link VehicleDetailDialogFragment} est affiché.</p>
     *
     * @param infoRequest L'objet contenant le numéro d'immatriculation cible.
     */
    private void getInfoVehicle(InfoRequest infoRequest){
        VehiclesCall.vehicleInfo(infoRequest, new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse res = (InfoResponse) response.body();
                Vehicle vehicle = res.createVehicles(false);

                sharedViewModel.addVehicle(vehicle);

                VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                dialog.show(getChildFragmentManager(), "detail");
            }

            @Override
            public void onResponseFailure(Response response) {
                Toast.makeText(getContext(), R.string.existence_plate, Toast.LENGTH_SHORT).show();
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(requireActivity(), SignInActivity.class);
                    requireActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Toast.makeText(getContext(), "Erreur lors de l'envoie de la requête : "+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Génère un fichier JPEG compressé et redimensionné à partir d'une {@link Uri} source.
     *
     * <p>Cette méthode effectue une optimisation en plusieurs étapes pour minimiser
     * l'empreinte mémoire (RAM) et le poids du fichier final :
     * <ol>
     * <li><strong>Pré-lecture :</strong> Analyse des dimensions de l'image sans chargement en mémoire via {@code inJustDecodeBounds}.</li>
     * <li><strong>Sous-échantillonnage :</strong> Calcul du {@code inSampleSize} pour décoder l'image à une résolution proche de la cible.</li>
     * <li><strong>Redimensionnement :</strong> Ajustement précis aux dimensions {@link #UPLOAD_MAX_DIMENSION}.</li>
     * <li><strong>Persistance :</strong> Compression au format JPEG avec une qualité de {@link #UPLOAD_JPEG_QUALITY} dans un fichier temporaire.</li>
     * </ol>
     * </p>
     * <p>Le fichier résultant est stocké dans le cache interne de l'application via {@link android.content.Context#getCacheDir()}
     * avec un nom unique (UUID) pour éviter les collisions.</p>
     *
     * @param uri L'{@link Uri} de l'image source (galerie ou caméra).
     * @return Un objet {@link File} pointant vers l'image optimisée, ou {@code null} en cas d'erreur d'E/S ou de décodage.
     * @see BitmapFactory.Options#inSampleSize
     * @see Bitmap#compress(Bitmap.CompressFormat, int, java.io.OutputStream)
     */
    private File getOptimizedJpegFromUri(Uri uri) {
        try {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            try (InputStream boundsInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (boundsInputStream == null) {
                    return null;
                }
                BitmapFactory.decodeStream(boundsInputStream, null, boundsOptions);
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, UPLOAD_MAX_DIMENSION, UPLOAD_MAX_DIMENSION);
            Bitmap decodedBitmap;
            try (InputStream decodeInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (decodeInputStream == null) {
                    return null;
                }
                decodedBitmap = BitmapFactory.decodeStream(decodeInputStream, null, decodeOptions);
            }

            if (decodedBitmap == null) {
                return null;
            }

            Bitmap bitmapForUpload = resizeBitmapIfNeeded(decodedBitmap, UPLOAD_MAX_DIMENSION);

            File tempFile = new File(requireContext().getCacheDir(), "upload_" + UUID.randomUUID() + ".jpg");
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                bitmapForUpload.compress(Bitmap.CompressFormat.JPEG, UPLOAD_JPEG_QUALITY, outputStream);
                outputStream.flush();
            }

            if (bitmapForUpload != decodedBitmap) {
                bitmapForUpload.recycle();
            }
            decodedBitmap.recycle();

            return tempFile;
        }
        catch(IOException e) {
            Log.e(TAG, "Unable to optimize image before upload", e);
            return null;
        }
    }

    /**
     * Récupère un {@link Bitmap} optimisé depuis une {@link Uri} locale.
     * * <p>Applique un sous-échantillonnage (InSampleSize) pour réduire l'empreinte mémoire
     * avant le chargement complet du fichier.</p>
     *
     * @param uri L'URI de l'image source.
     * @return Un bitmap redimensionné ou {@code null} en cas d'erreur de lecture.
     */
    private Bitmap getOptimizedBitmapFromUri(Uri uri) {
        try {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            try (InputStream boundsInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (boundsInputStream == null) return null;
                BitmapFactory.decodeStream(boundsInputStream, null, boundsOptions);
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = calculateInSampleSize(boundsOptions, UPLOAD_MAX_DIMENSION, UPLOAD_MAX_DIMENSION);
            Bitmap decodedBitmap;
            try (InputStream decodeInputStream = requireContext().getContentResolver().openInputStream(uri)) {
                if (decodeInputStream == null) return null;
                decodedBitmap = BitmapFactory.decodeStream(decodeInputStream, null, decodeOptions);
            }

            if (decodedBitmap == null) return null;

            return resizeBitmapIfNeeded(decodedBitmap, UPLOAD_MAX_DIMENSION);
        } catch (IOException e) {
            Log.e(TAG, "Unable to load bitmap", e);
            return null;
        }
    }

    /**
     * Redimensionne le bitmap pour que sa plus grande dimension ne dépasse pas le maximum spécifié.
     *
     * @param bitmap       Le bitmap source.
     * @param maxDimension La dimension maximale autorisée (px).
     * @return Un nouveau {@link Bitmap} redimensionné ou le bitmap original.
     */
    private Bitmap resizeBitmapIfNeeded(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int largestDimension = Math.max(width, height);

        if (largestDimension <= maxDimension) {
            return bitmap;
        }

        float ratio = (float) maxDimension / largestDimension;
        int resizedWidth = Math.max(1, Math.round(width * ratio));
        int resizedHeight = Math.max(1, Math.round(height * ratio));

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true);
    }

    /**
     * Calcule le ratio de sous-échantillonnage idéal pour décoder une image.
     *
     * @param options   Options contenant les dimensions réelles de l'image.
     * @param reqWidth  Largeur souhaitée.
     * @param reqHeight Hauteur souhaitée.
     * @return La valeur {@code inSampleSize} (puissance de 2).
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }

        return Math.max(1, inSampleSize);
    }

    /**
     * Met à jour l'état d'activation de l'interface utilisateur pendant un traitement.
     *
     * @param isLoading {@code true} si un traitement est en cours, {@code false} sinon.
     */
    private void setLoading(boolean isLoading) {
        if (btnSearch != null) {
            btnSearch.setEnabled(!isLoading);
        }
    }

    /**
     * Exécute une tâche sur le thread principal après avoir vérifié que le fragment est toujours actif.
     *
     * @param action Le {@link Runnable} à exécuter.
     */
    private void runOnMainThread(Runnable action) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }

    /**
     * Ferme l'exécuteur de tâches et libère les ressources ML à la destruction du fragment.
     */
    @Override
    public void onDestroy() {
        imageExecutor.shutdown();
        super.onDestroy();
    }

    /**
     * Nettoie et extrait une plaque valide à partir d'une chaîne brute issue de l'OCR.
     * * <p>Supprime les caractères spéciaux et recherche une correspondance avec
     * le motif standard AA-123-BB.</p>
     *
     * @param noisyText Le texte brut détecté.
     * @return La chaîne formatée de la plaque ou {@code null} si aucune plaque valide n'est trouvée.
     */
    private String extractPlate(String noisyText) {
        if (noisyText == null) return null;

        // Nettoyage : majuscules + suppression des caractères non alphanumériques
        String cleaned = noisyText.toUpperCase().replaceAll("[^A-Z0-9]", "");

        // Regex plaque française : 2 lettres + 3 chiffres + 2 lettres (ex: AB123CD)
        Pattern pattern = Pattern.compile("[A-Z]{2}\\d{3}[A-Z]{2}");
        Matcher matcher = pattern.matcher(cleaned);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
