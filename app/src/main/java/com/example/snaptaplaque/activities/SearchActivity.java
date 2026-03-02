package com.example.snaptaplaque.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.snaptaplaque.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.snaptaplaque.adapters.ViewPageAdapter;

/**
 * Activité principale de l'application servant de point d'entrée après le lancement.
 * <p>
 * Cette activité orchestre la navigation entre les trois écrans principaux de l'application
 * à l'aide d'un {@link ViewPager2} couplé à une {@link BottomNavigationView} :
 * <ol>
 *     <li><strong>Historique</strong> (position 0) — Liste des plaques scannées</li>
 *     <li><strong>Recherche</strong> (position 1) — Scan / saisie d'une plaque d'immatriculation</li>
 *     <li><strong>Profil</strong> (position 2) — Informations et préférences de l'utilisateur</li>
 * </ol>
 * </p>
 *
 * <h3>Synchronisation navigation ↔ pages</h3>
 * <ul>
 *     <li>Un clic sur un élément de la {@link BottomNavigationView} fait défiler
 *     le {@link ViewPager2} vers la page correspondante.</li>
 *     <li>Un glissement latéral (swipe) dans le {@link ViewPager2} met à jour
 *     l'élément sélectionné de la {@link BottomNavigationView}.</li>
 * </ul>
 *
 * <p>Au démarrage, l'écran de recherche (position 1) est affiché par défaut.</p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see AppCompatActivity
 * @see ViewPageAdapter
 * @see ViewPager2
 * @see BottomNavigationView
 */
public class SearchActivity extends AppCompatActivity {

    /**
     * Le composant {@link ViewPager2} permettant la navigation par glissement entre les fragments.
     */
    private ViewPager2 viewPager;

    /**
     * La barre de navigation inférieure permettant de sélectionner un écran via un clic.
     */
    private BottomNavigationView bottomNav;

    /**
     * Appelé lors de la création de l'activité.
     * <p>
     * Initialise les composants graphiques et configure les interactions suivantes :
     * <ol>
     *     <li>Gonfle le layout {@code activity_search}.</li>
     *     <li>Récupère les références du {@link ViewPager2} et de la {@link BottomNavigationView}.</li>
     *     <li>Associe un {@link ViewPageAdapter} au {@link ViewPager2} pour gérer
     *     la création des fragments.</li>
     *     <li>Configure le listener de sélection sur la {@link BottomNavigationView}
     *     pour synchroniser le changement de page.</li>
     *     <li>Enregistre un {@link ViewPager2.OnPageChangeCallback} pour synchroniser
     *     l'icône active de la barre de navigation lors d'un swipe.</li>
     *     <li>Positionne la page initiale sur l'écran de recherche (index 1).</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState le {@link Bundle} contenant l'état précédemment sauvegardé
     *                           de l'activité, ou {@code null} lors d'une première création
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);

        // Configuration de l'adaptateur du ViewPager (Fragment pour chaque page)
        ViewPageAdapter adapter = new ViewPageAdapter(this);
        viewPager.setAdapter(adapter);

        // Clic sur un bouton de la Sidebar -> change la page
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) viewPager.setCurrentItem(0);
            else if (id == R.id.nav_search) viewPager.setCurrentItem(1);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(2);
            return true;
        });

        // Coche l'icône Search dans la Sidebar
        bottomNav.setSelectedItemId(R.id.nav_search);

        // Slide de la page -> Change l'icône actif en bas
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            /**
             * Appelé lorsqu'une nouvelle page est sélectionnée suite à un swipe.
             * <p>
             * Met à jour l'élément coché dans la {@link BottomNavigationView}
             * pour refléter la page actuellement affichée.
             * </p>
             *
             * @param position l'index de la nouvelle page sélectionnée (0, 1 ou 2)
             */
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });

        // Force le ViewPager à aller sur l'index 1 (Search) au démarrage
        viewPager.setCurrentItem(1, false);
    }
}