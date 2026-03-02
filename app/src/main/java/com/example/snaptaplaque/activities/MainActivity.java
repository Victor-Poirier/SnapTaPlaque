package com.example.snaptaplaque.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.ViewPageAdapter;
import com.example.snaptaplaque.models.Profil;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

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
public class MainActivity extends AppCompatActivity {

    /**
     * Composant de pagination permettant le swipe entre les fragments.
     */
    private ViewPager2 viewPager;

    /**
     * Barre de navigation inférieure permettant la sélection directe d'un fragment.
     */
    private BottomNavigationView bottomNav;

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

        // Données fictives initiales pour l'historique
        List<Vehicle> initialVehicles = new ArrayList<>();
        initialVehicles.add(new Vehicle("DR-593-DE", "Renault Laguna 3 Coupé Intens 2.0 dCi 175", true));
        initialVehicles.add(new Vehicle("AB-123-CD", "Peugeot 208 1.2 PureTech 110", false));
        initialVehicles.add(new Vehicle("EF-456-GH", "Citroën C3 Aircross Shine 1.5 BlueHDi 120", false));
        initialVehicles.add(new Vehicle("IJ-789-KL", "Volkswagen Golf 7 1.4 TSI 150", false));
        initialVehicles.add(new Vehicle("MN-012-OP", "Ford Fiesta 1.0 EcoBoost 100", false));
        sharedViewModel.setVehicles(initialVehicles);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);

        ViewPageAdapter adapter = new ViewPageAdapter(this);
        viewPager.setAdapter(adapter);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) viewPager.setCurrentItem(0);
            else if (id == R.id.nav_search) viewPager.setCurrentItem(1);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(2);
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_search);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            /**
             * Appelé lorsque la page sélectionnée change (par swipe ou programmatiquement).
             *
             * <p>Met à jour l'élément coché dans la {@link BottomNavigationView}
             * pour refléter la page actuellement visible dans le {@link ViewPager2}.</p>
             *
             * @param position l'index de la nouvelle page sélectionnée (0, 1 ou 2)
             */
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });

        viewPager.setCurrentItem(1, false);
    }
}