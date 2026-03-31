package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.InputSectionAdapter;

/**
 * Fragment dédié à la recherche et au scan de plaques d'immatriculation.
 *
 * <p>Ce fragment permet à l'utilisateur de saisir ou scanner une plaque
 * d'immatriculation afin d'obtenir les informations associées au véhicule.
 * Chaque véhicule scanné est automatiquement ajouté à l'historique de recherche
 * via le {@link com.example.snaptaplaque.viewmodels.SharedViewModel} partagé au niveau de l'activité hôte.</p>
 *
 * <p>Le {@code SharedViewModel} assure la communication avec les autres fragments :
 * <ul>
 *     <li>{@link HistoryFragment} — affiche la liste complète des véhicules scannés</li>
 *     <li>{@link ProfileFragment} — affiche uniquement les véhicules marqués comme favoris</li>
 * </ul>
 * </p>
 *
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 * @see HistoryFragment
 * @see ProfileFragment
 */
public class SearchFragment extends Fragment {

    /** Composant permettant la navigation par balayage vertical entre les sections de saisie. */
    private ViewPager2 viewPagerSearch;

    /** Icône représentant le mode Scan (Index 0). */
    private ImageView indicatorScan;
    /** Icône représentant le mode Saisie manuelle (Index 1). */
    private ImageView indicatorWheel;
    /** Icône représentant le mode Saisie vocale (Index 2). */
    private ImageView indicatorVocal;

    /**
     * Initialise la vue du fragment et configure le {@link com.example.snaptaplaque.viewmodels.SharedViewModel}.
     *
     * <p>Gonfle le layout {@code fragment_search.xml} et récupère l'instance
     * du {@link com.example.snaptaplaque.viewmodels.SharedViewModel} scopée à l'activité parente.
     * La logique de recherche (champ de saisie,
     * bouton, appel API) est à décommenter et adapter selon le layout utilisé.</p>
     *
     * @param inflater           le {@link LayoutInflater} utilisé pour gonfler la vue
     * @param container          le conteneur parent dans lequel la vue sera insérée
     * @param savedInstanceState l'état précédemment sauvegardé du fragment, ou {@code null}
     * @return la vue racine du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        viewPagerSearch = view.findViewById(R.id.viewPagerSearch);
        indicatorScan = view.findViewById(R.id.indicatorScan);
        indicatorWheel = view.findViewById(R.id.indicatorWheel);
        indicatorVocal = view.findViewById(R.id.indicatorVocal);


        // Configuration du ViewPager
        if (viewPagerSearch != null) {
            // Définit le swipe en mode vertical
            viewPagerSearch.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
            InputSectionAdapter adapter = new InputSectionAdapter(this);
            viewPagerSearch.setAdapter(adapter);

            // Synchronisation : Swipe -> Mise à jour visuelle des cercles
            viewPagerSearch.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                /**
                 * Déclenché lorsqu'une nouvelle page devient active.
                 * @param position Index de la nouvelle page sélectionnée.
                 */
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateIndicators(position);
                }
            });
        }
        // Synchronisation : Clic sur cercle -> Changement de page
        if (indicatorScan != null) {
            indicatorScan.setOnClickListener(v -> selectPage(0));
        }
        if (indicatorWheel != null) {
            indicatorWheel.setOnClickListener(v -> selectPage(1));
        }
        if (indicatorVocal != null) {
            indicatorVocal.setOnClickListener(v -> selectPage(2));
        }
        return view;
    }

    /**
     * Change programmatiquement la page affichée par le {@link ViewPager2}.
     *
     * @param index L'index de la destination (0 pour Scan, 1 pour Wheel, 2 pour Vocal).
     */
    private void selectPage(int index) {
        if (viewPagerSearch != null) {
            viewPagerSearch.setCurrentItem(index, true);
        }
    }

    /**
     * Met à jour l'état visuel des indicateurs de navigation et anime la barre de sélection.
     *
     * <p>Cette méthode gère deux aspects visuels :
     * 1. Modifie l'état {@code selected} des icônes pour activer les sélecteurs de couleur (XML).
     * 2. Calcule la position verticale de la {@code movingBar} pour l'aligner avec l'icône active.</p>
     *
     * @param position L'index de la page actuellement active.
     */
    private void updateIndicators(int position) {
        if (indicatorScan != null && indicatorWheel != null && indicatorVocal != null) {
            indicatorScan.setSelected(position == 0);
            indicatorWheel.setSelected(position == 1);
            indicatorVocal.setSelected(position == 2);

            View movingBar = getView().findViewById(R.id.movingBar);
            ImageView targetIcon;

            if (position == 0) targetIcon = indicatorScan;
            else if (position == 1) targetIcon = indicatorWheel;
            else targetIcon = indicatorVocal;

            if (movingBar != null) {
                targetIcon.post(() -> {
                    // On calcule le centre vertical de l'icône pour y aligner le centre de la barre
                    float targetY = targetIcon.getY() + (targetIcon.getHeight() / 2f) - (movingBar.getHeight() / 2f);

                    movingBar.animate()
                            .translationY(targetY)
                            .setDuration(200)
                            .start();
                });
            }
        }
    }
}