package com.example.snaptaplaque.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Activité de base dont héritent toutes les activités de l'application SnapTaPlaque.
 *
 * <p>Fournit un comportement commun d'affichage en plein écran en masquant
 * les barres système (barre de statut et barre de navigation) via
 * {@link WindowInsetsControllerCompat}.</p>
 *
 * <p>Le masquage est appliqué dans {@link #onPostCreate(Bundle)} plutôt que dans
 * {@link #onCreate(Bundle)} afin de garantir que la {@code DecorView} est
 * pleinement initialisée avant l'appel à {@link #hideSystemUI()}.</p>
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * Initialise l'activité de base.
     *
     * <p>Ne définit pas de layout propre : chaque sous-classe est responsable
     * d'appeler {@link #setContentView(int)} avec son propre layout.</p>
     *
     * @param savedInstanceState état sauvegardé de l'activité, ou {@code null}
     *                           lors du premier lancement
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Applique le mode plein écran une fois la création de l'activité terminée.
     *
     * <p>Cette méthode est préférée à {@link #onCreate(Bundle)} pour l'appel à
     * {@link #hideSystemUI()}, car la {@code DecorView} est garantie d'être prête
     * à ce stade, évitant ainsi un {@link NullPointerException} sur
     * {@code getInsetsController()}.</p>
     *
     * @param savedInstanceState état sauvegardé de l'activité, ou {@code null}
     *                           lors du premier lancement
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // On applique le plein écran ici pour s'assurer que la DecorView est prête,
        // évitant ainsi un NullPointerException sur getInsetsController().
        hideSystemUI();
    }

    /**
     * Masque les barres système (barre de statut et barre de navigation) pour
     * afficher l'activité en plein écran.
     *
     * <p>Le comportement {@link WindowInsetsControllerCompat#BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE}
     * permet à l'utilisateur de faire réapparaître temporairement les barres
     * système par un glissement depuis le bord de l'écran, sans quitter
     * le mode plein écran de façon permanente.</p>
     *
     * <p>Si le {@link WindowInsetsControllerCompat} est {@code null} (cas rare
     * selon la version d'Android ou l'état de la fenêtre), la méthode
     * s'interrompt silencieusement sans lever d'exception.</p>
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), decorView);
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}