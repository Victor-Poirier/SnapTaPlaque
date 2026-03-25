package com.example.snaptaplaque.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * Gestionnaire de reconnaissance de plaque (Detection YOLO + OCR ML Kit).
 *
 * Pré-requis :
 * 1. Placer le fichier 'model.onnx' dans app/src/main/assets/
 * 2. Vérifier que le format de sortie du modèle correspond (YOLOv12 : [1, mul_boxes, 5+cls] ou transposé)
 */
public class LicensePlateRecognizer {

    private static final String TAG = "LicensePlateRecognizer";
    private static final String MODEL_FILE = "model.onnx";
    private static final int INPUT_SIZE = 640;
    private static final float CONFIDENCE_THRESHOLD = 0.45f;
    private static final float IOU_THRESHOLD = 0.45f;

    private OrtEnvironment ortEnvironment;
    private OrtSession ortSession;
    private final TextRecognizer ocrRecognizer;
    private final Context context;

    public LicensePlateRecognizer(Context context) {
        this.context = context;
        this.ocrRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public static class PlateResult {
        public final String text;
        public final float confidence;
        public final Rect box;

        public PlateResult(String text, float confidence, Rect box) {
            this.text = text;
            this.confidence = confidence;
            this.box = box;
        }

        @Override
        public String toString() {
            return "PlateResult{text='" + text + "', confidence=" + confidence + ", box=" + box + '}';
        }
    }

    private static class DetectionResult {
        final float x1, y1, x2, y2;
        final float score;
        final int classId;

        public DetectionResult(float x1, float y1, float x2, float y2, float score, int classId) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.score = score;
            this.classId = classId;
        }
    }

    /**
     * Initialise le modèle ONNX Runtime. Doit être appelé hors du thread principal.
     */
    public void init() throws Exception {
        if (ortSession == null) {
            ortEnvironment = OrtEnvironment.getEnvironment();
            byte[] modelBytes = readAssetFile(context, MODEL_FILE);
            ortSession = ortEnvironment.createSession(modelBytes, new OrtSession.SessionOptions());
            Log.d(TAG, "ONNX Runtime Initialized");
        }
    }

    /**
     * Traite l'image fournie pour détecter et lire les plaques.
     * Doit être appelé hors du thread principal.
     */
    public List<PlateResult> processImage(Bitmap bitmap) {
        try {
            if (ortSession == null) init();

            // 1. Détection YOLO
            List<DetectionResult> detections = detect(bitmap);
            if (detections.isEmpty()) {
                return Collections.emptyList();
            }

            List<PlateResult> finalResults = new ArrayList<>();

            // 2. OCR sur chaque détection
            for (DetectionResult det : detections) {
                Bitmap crop = cropBitmap(bitmap, det);
                if (crop != null) {
                    String ocrText = runOcr(crop);
                    if (ocrText != null && !ocrText.trim().isEmpty()) {
                        // Nettoyage basique (garder alphanum, retirer espaces)
                        String cleanText = ocrText.replaceAll("\\s+", "").toUpperCase();
                        // Filtre basique: 2 chars min
                        if (cleanText.length() >= 2) {
                            finalResults.add(new PlateResult(
                                    cleanText,
                                    det.score,
                                    new Rect((int) det.x1, (int) det.y1, (int) det.x2, (int) det.y2)
                            ));
                        }
                    }
                }
            }

            // Trier par confiance décroissante
            Collections.sort(finalResults, new Comparator<PlateResult>() {
                @Override
                public int compare(PlateResult o1, PlateResult o2) {
                    return Float.compare(o2.confidence, o1.confidence);
                }
            });

            return finalResults;

        } catch (Exception e) {
            Log.e(TAG, "Error during processing", e);
            return Collections.emptyList();
        }
    }

    private List<DetectionResult> detect(Bitmap bitmap) throws OrtException {
        // Pré-traitement
        PreprocessResult preprocessResult = preprocess(bitmap);
        OnnxTensor inputTensor = preprocessResult.tensor;

        // Inférence
        String inputName = ortSession.getInputNames().iterator().next();
        OrtSession.Result result = ortSession.run(Collections.singletonMap(inputName, inputTensor));

        // Post-traitement
        OnnxTensor outputTensor = (OnnxTensor) result.get(0);
        FloatBuffer floatBuffer = outputTensor.getFloatBuffer();
        long[] shape = outputTensor.getInfo().getShape(); // [1, 5, 8400] ou [1, 8400, 5]

        List<DetectionResult> rawBoxes = parseYoloOutput(floatBuffer, shape, preprocessResult.scaleX, preprocessResult.scaleY);

        // Nettoyage temporaire (close tensors)
        inputTensor.close();
        result.close();
        outputTensor.close();

        return nms(rawBoxes);
    }

    private static class PreprocessResult {
        OnnxTensor tensor;
        float scaleX, scaleY;
    }

    private PreprocessResult preprocess(Bitmap bitmap) throws OrtException {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        float scaleX = (float) originalWidth / INPUT_SIZE;
        float scaleY = (float) originalHeight / INPUT_SIZE;

        FloatBuffer floatBuffer = FloatBuffer.allocate(3 * INPUT_SIZE * INPUT_SIZE);
        floatBuffer.rewind();

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        scaledBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        // Ordonnancement CWH (Channel-first) : RRR...GGG...BBB...
        // Nous allons utiliser 3 boucles séparées ou calculer l'offset
        // Offset : [0..SIZE*SIZE-1] -> R, [SIZE*SIZE..2*SIZE*SIZE-1] -> G, ...

        // Approche avec array temporaire pour simplifier l'écriture dans le buffer
        float[] imgData = new float[3 * INPUT_SIZE * INPUT_SIZE];
        int stride = INPUT_SIZE * INPUT_SIZE;

        for (int i = 0; i < stride; i++) {
            int pixel = pixels[i];
            imgData[i] = Color.red(pixel) / 255.0f;             // R
            imgData[i + stride] = Color.green(pixel) / 255.0f;      // G
            imgData[i + 2 * stride] = Color.blue(pixel) / 255.0f;   // B
        }

        floatBuffer.put(imgData);
        floatBuffer.flip();

        OnnxTensor tensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, new long[]{1, 3, INPUT_SIZE, INPUT_SIZE});

        PreprocessResult res = new PreprocessResult();
        res.tensor = tensor;
        res.scaleX = scaleX;
        res.scaleY = scaleY;
        return res;
    }

    private List<DetectionResult> parseYoloOutput(FloatBuffer data, long[] shape, float scaleX, float scaleY) {
        List<DetectionResult> results = new ArrayList<>();

        int numBoxes;
        int numFeatures;
        boolean isTranspose; // true si [1, channels, anchors]

        // Détection format : [1, 5, 8400] vs [1, 8400, 5]
        if (shape[1] < shape[2]) {
            // shape[1] is smaller (features, e.g., 5), shape[2] is larger (boxes, e.g., 8400)
            numFeatures = (int) shape[1];
            numBoxes = (int) shape[2];
            isTranspose = true;
        } else {
            // shape[1] is larger (boxes), shape[2] is smaller (features)
            numBoxes = (int) shape[1];
            numFeatures = (int) shape[2];
            isTranspose = false;
        }

        for (int i = 0; i < numBoxes; i++) {
            // Dans YOLOv8 export normal, classes commencent à index 4.
            // Score confidence = max des probas de classe.
            // Si 1 seule classe, score est à index 4.
            float score = getVal(data, i, 4, numFeatures, numBoxes, isTranspose);

            if (score > CONFIDENCE_THRESHOLD) {
                float cx = getVal(data, i, 0, numFeatures, numBoxes, isTranspose);
                float cy = getVal(data, i, 1, numFeatures, numBoxes, isTranspose);
                float w = getVal(data, i, 2, numFeatures, numBoxes, isTranspose);
                float h = getVal(data, i, 3, numFeatures, numBoxes, isTranspose);

                float x1 = (cx - w / 2) * scaleX;
                float y1 = (cy - h / 2) * scaleY;
                float x2 = (cx + w / 2) * scaleX;
                float y2 = (cy + h / 2) * scaleY;

                results.add(new DetectionResult(x1, y1, x2, y2, score, 0));
            }
        }
        return results;
    }

    private float getVal(FloatBuffer data, int boxIdx, int attrIdx, int numFeatures, int numBoxes, boolean isTranspose) {
        if (isTranspose) {
            // [1, features, boxes] -> acces [0, attrIdx, boxIdx]
            // buffer index = attrIdx * numBoxes + boxIdx
            return data.get(attrIdx * numBoxes + boxIdx);
        } else {
            // [1, boxes, features]
            // buffer index = boxIdx * numFeatures + attrIdx
            return data.get(boxIdx * numFeatures + attrIdx);
        }
    }

    private List<DetectionResult> nms(List<DetectionResult> boxes) {
        if (boxes.isEmpty()) return Collections.emptyList();

        // Trier par score décroissant
        Collections.sort(boxes, new Comparator<DetectionResult>() {
            @Override
            public int compare(DetectionResult o1, DetectionResult o2) {
                return Float.compare(o2.score, o1.score);
            }
        });

        List<DetectionResult> selected = new ArrayList<>();
        boolean[] active = new boolean[boxes.size()];
        for (int i = 0; i < boxes.size(); i++) active[i] = true;

        for (int i = 0; i < boxes.size(); i++) {
            if (active[i]) {
                DetectionResult boxA = boxes.get(i);
                selected.add(boxA);

                for (int j = i + 1; j < boxes.size(); j++) {
                    if (active[j]) {
                        DetectionResult boxB = boxes.get(j);
                        float iou = calculateIoU(boxA, boxB);
                        if (iou > IOU_THRESHOLD) {
                            active[j] = false;
                        }
                    }
                }
            }
        }
        return selected;
    }

    private float calculateIoU(DetectionResult a, DetectionResult b) {
        float xA = Math.max(a.x1, b.x1);
        float yA = Math.max(a.y1, b.y1);
        float xB = Math.min(a.x2, b.x2);
        float yB = Math.min(a.y2, b.y2);

        float interW = Math.max(0f, xB - xA);
        float interH = Math.max(0f, yB - yA);
        float interArea = interW * interH;

        float boxAArea = (a.x2 - a.x1) * (a.y2 - a.y1);
        float boxBArea = (b.x2 - b.x1) * (b.y2 - b.y1);

        return interArea / (boxAArea + boxBArea - interArea);
    }

    private Bitmap cropBitmap(Bitmap source, DetectionResult det) {
        int x = Math.max(0, (int) det.x1);
        int y = Math.max(0, (int) det.y1);
        int w = Math.min(source.getWidth() - x, (int) (det.x2 - det.x1));
        int h = Math.min(source.getHeight() - y, (int) (det.y2 - det.y1));

        if (w <= 0 || h <= 0) return null;
        return Bitmap.createBitmap(source, x, y, w, h);
    }

    private String runOcr(Bitmap bitmap) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Task<Text> result = ocrRecognizer.process(image);
            Text text = Tasks.await(result); // Bloquant, car on est déjà dans un thread background (appelé par processImage)
            return text.getText();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "OCR failed", e);
            return null;
        }
    }

    private byte[] readAssetFile(Context context, String fileName) throws Exception {
        try (InputStream is = context.getAssets().open(fileName);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }
}

