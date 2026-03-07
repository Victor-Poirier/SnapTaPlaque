package com.example.snaptaplaque.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.InputSectionAdapter;
import com.example.snaptaplaque.models.Photo;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

/**
 * Fragment dédié à la recherche et au scan de plaques d'immatriculation.
 *
 * <p>Ce fragment permet à l'utilisateur de saisir ou scanner une plaque
 * d'immatriculation afin d'obtenir les informations associées au véhicule.
 * Chaque véhicule scanné est automatiquement ajouté à l'historique de recherche
 * via le {@link SharedViewModel} partagé au niveau de l'activité hôte.</p>
 *
 * <p>Le {@code SharedViewModel} assure la communication avec les autres fragments :
 * <ul>
 *     <li>{@link HistoryFragment} — affiche la liste complète des véhicules scannés</li>
 *     <li>{@link ProfileFragment} — affiche uniquement les véhicules marqués comme favoris</li>
 * </ul>
 * </p>
 *
 * @see SharedViewModel #addVehicle(Vehicle)
 * @see HistoryFragment
 * @see ProfileFragment
 */
public class SearchFragment extends Fragment {

    private ViewPager2 viewPagerSearch;

    // Les indicateurs
    private ImageView indicatorScan;
    private ImageView indicatorWheel;
    private ImageView indicatorVocal;

    /**
     * Initialise la vue du fragment et configure le {@link SharedViewModel}.
     *
     * <p>Gonfle le layout {@code fragment_search.xml} et récupère l'instance
     * du {@link SharedViewModel} scopée à l'activité parente via
     * {@link ViewModelProvider}. La logique de recherche (champ de saisie,
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
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateIndicators(view, position);
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
     * Change la page du ViewPager
     */
    private void selectPage(int index) {
        if (viewPagerSearch != null) {
            viewPagerSearch.setCurrentItem(index, true);
        }
    }

    /**
     * Met à jour l'état visuel (Selected) des indicateurs
     */
    private void updateIndicators(View view, int position) {
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