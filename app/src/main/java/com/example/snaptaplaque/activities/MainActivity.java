package com.example.snaptaplaque.activities;

import android.os.Bundle;

import android.widget.ImageView;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.ViewPageAdapter;
import com.example.snaptaplaque.models.Profil;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.viewmodels.SharedViewModel;


/**
 * Activité principale de l'application SnapTaPlaque.
 *
 * <p>Cette activité sert de conteneur hôte pour les trois fragments principaux
 * de l'application, gérés via un {@link ViewPager2} et une
 * {@link BottomNavigationView} pour la navigation :
 * <ol>
 *     <li>{@link com.example.snaptaplaque.fragments.HistoryFragment} — Historique des véhicules scannés</li>
 *     <li>{@link com.example.snaptaplaque.fragments.SearchFragment} — Recherche et scan de plaques d'immatriculation</li>
 *     <li>{@link com.example.snaptaplaque.fragments.ProfileFragment} — Profil utilisateur et véhicules favoris</li>
 * </ol>
 * </p>
 *
 * <p>Au démarrage, l'activité initialise le {@link SharedViewModel} partagé entre
 * les fragments. Ce ViewModel centralise :
 * <ul>
 *     <li>Le {@link Profil} de l'utilisateur courant</li>
 *     <li>La liste complète des {@link Vehicle} scannés (historique)</li>
 *     <li>La liste filtrée des véhicules marqués comme favoris</li>
 * </ul>
 * Les fragments accèdent à ce même ViewModel via
 * {@code new ViewModelProvider(requireActivity()).get(SharedViewModel.class)},
 * garantissant une source de données unique et une synchronisation automatique.</p>
 *
 * <p>La navigation entre les pages est bidirectionnelle :
 * <ul>
 *     <li>Le swipe horizontal dans le {@code ViewPager2} met à jour l'élément sélectionné
 *         dans la {@code BottomNavigationView}</li>
 *     <li>Le clic sur un onglet de la {@code BottomNavigationView} change la page
 *         affichée dans le {@code ViewPager2}</li>
 * </ul>
 * La page affichée par défaut au lancement est la page de recherche (position 1).</p>
 * @see SharedViewModel
 * @see ViewPageAdapter
 * @see com.example.snaptaplaque.fragments.HistoryFragment
 * @see com.example.snaptaplaque.fragments.SearchFragment
 * @see com.example.snaptaplaque.fragments.ProfileFragment
 */
public class MainActivity extends BaseActivity {

    /**
     * Composant de pagination permettant le swipe entre les fragments.
     */
    private ViewPager2 viewPager;


    private ImageView circleHistory;
    private ImageView circleSearch;
    private ImageView circleProfile;

    /**
     * Initialise l'activité, configure la navigation et charge les données initiales.
     *
     * <p>Cette méthode effectue les opérations suivantes dans l'ordre :
     * <ol>
     *     <li>Gonfle le layout {@code activity_search.xml}</li>
     *     <li>Instancie le {@link SharedViewModel} scopé à cette activité</li>
     *     <li>Crée un {@link Profil} utilisateur par défaut et l'attache au ViewModel</li>
     *     <li>Charge une liste de {@link Vehicle} fictifs dans l'historique du ViewModel</li>
     *     <li>Configure le {@link ViewPager2} avec un {@link ViewPageAdapter}</li>
     *     <li>Lie la {@link BottomNavigationView} au {@code ViewPager2} pour synchroniser
     *         la navigation (clic sur onglet ↔ changement de page)</li>
     *     <li>Positionne l'affichage initial sur la page de recherche (index 1)</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState l'état précédemment sauvegardé de l'activité,
     *                           ou {@code null} s'il s'agit d'un premier lancement
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialise le ViewModel partagé
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        Profil profil = Profil.createProfil("testuser", "Test", "User", "password123", "user@email.fr");
        sharedViewModel.setProfil(profil);

        viewPager = findViewById(R.id.viewPager);
        circleHistory = findViewById(R.id.circleHistory);
        circleSearch = findViewById(R.id.circleSearch);
        circleProfile = findViewById(R.id.circleProfile);

        if (viewPager == null || circleHistory == null || circleSearch == null || circleProfile == null)
            return;

        // Configuration de l'adaptateur
        ViewPageAdapter adapter = new ViewPageAdapter(this);
        viewPager.setAdapter(adapter);

        // Clicks sur les cercles
        circleHistory.setOnClickListener(v -> selectTab(0));
        circleSearch.setOnClickListener(v -> selectTab(1));
        circleProfile.setOnClickListener(v -> selectTab(2));

        // Synchronisation ViewPager → Gauges
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSelection(position);
            }
        });

        // Positionnement initial sur "Search" (index 1)
        viewPager.setCurrentItem(1, false);
        updateSelection(1);
    }


    // Change de page
    private void selectTab(int index) {
        viewPager.setCurrentItem(index, true);
        updateSelection(index);
    }

    // Met à jour visuellement les gauges
    private void updateSelection(int selectedIndex) {

        circleHistory.setSelected(selectedIndex == 0);
        circleSearch.setSelected(selectedIndex == 1);
        circleProfile.setSelected(selectedIndex == 2);
    }
}