package com.example.snaptaplaque.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/*Importation des fragments*/
import com.example.snaptaplaque.fragments.HistoryFragment;
import com.example.snaptaplaque.fragments.ProfileFragment;
import com.example.snaptaplaque.fragments.SearchFragment;

/**
 * Adaptateur de pages pour le composant {@link androidx.viewpager2.widget.ViewPager2}.
 * <p>
 * Cette classe étend {@link FragmentStateAdapter} et gère la création et la navigation
 * entre les trois fragments principaux de l'application :
 * <ol>
 *     <li><strong>Position 0 :</strong> {@link HistoryFragment} — Historique des plaques scannées</li>
 *     <li><strong>Position 1 :</strong> {@link SearchFragment} — Recherche de véhicules</li>
 *     <li><strong>Position 2 :</strong> {@link ProfileFragment} — Profil de l'utilisateur</li>
 * </ol>
 * </p>
 * <p>
 * Le {@code ViewPager2} permet à l'utilisateur de naviguer entre ces écrans
 * par glissement latéral (swipe) ou via un composant de navigation associé
 * (ex. : {@code BottomNavigationView}, {@code TabLayout}).
 * </p>
 *
 * @author SnapTaPlaque's Team
 * @version 1.0
 * @see FragmentStateAdapter
 * @see HistoryFragment
 * @see SearchFragment
 * @see ProfileFragment
 */
public class ViewPageAdapter extends FragmentStateAdapter {

    /**
     * Le nombre total de pages (fragments) gérées par cet adaptateur.
     */
    private static final int NUM_PAGES = 3;

    /**
     * Construit un nouvel adaptateur de pages lié à l'activité hôte spécifiée.
     *
     * @param fragmentActivity la {@link FragmentActivity} hôte qui héberge le
     *                         {@link androidx.viewpager2.widget.ViewPager2} ;
     *                         ne doit pas être {@code null}
     */
    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Crée et retourne le {@link Fragment} correspondant à la position demandée.
     * <p>
     * La correspondance position → fragment est la suivante :
     * <ul>
     *     <li>{@code 0} → {@link HistoryFragment} (Historique)</li>
     *     <li>{@code 1} → {@link SearchFragment} (Recherche)</li>
     *     <li>{@code 2} → {@link ProfileFragment} (Profil)</li>
     *     <li>Toute autre valeur → {@link SearchFragment} (par défaut)</li>
     * </ul>
     * </p>
     *
     * @param position la position de la page dans le {@code ViewPager2} (index basé sur 0)
     * @return une nouvelle instance du {@link Fragment} correspondant à la position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retourne le Fragment correspondant à la position
        switch (position) {
            case 0:
                return new HistoryFragment(); // Historique
            case 1:
                return new SearchFragment();  // Recherche
            case 2:
                return new ProfileFragment(); // Profil
            default:
                return new SearchFragment(); // Par défaut
        }
    }

    /**
     * Retourne le nombre total de pages gérées par cet adaptateur.
     *
     * @return le nombre de fragments disponibles, soit {@value #NUM_PAGES}
     */
    @Override
    public int getItemCount() {
        return NUM_PAGES; // Nombre total de pages
    }
}