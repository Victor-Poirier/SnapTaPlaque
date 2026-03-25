package com.example.snaptaplaque.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/*Importation des fragments*/
import com.example.snaptaplaque.fragments.PictureFragment;
import com.example.snaptaplaque.fragments.SearchFragment;
import com.example.snaptaplaque.fragments.WheelFragment;
import com.example.snaptaplaque.fragments.VocalFragment;

/**
 * Adaptateur de choix de saisie pour le composant {@link androidx.viewpager2.widget.ViewPager2}.
 *
 * <p>Cette classe étend {@link FragmentStateAdapter} et gère la création et la navigation
 * entre les trois fragments principaux de l'application :
 * <ol>
 *     <li><strong>Position 0 :</strong> {@link PictureFragment} — Saisie de la plaque par photo</li>
 *     <li><strong>Position 1 :</strong> {@link WheelFragment} — Saisie de la plaque par roulette</li>
 *     <li><strong>Position 2 :</strong> {@link VocalFragment} — Saisie de la plaque par commande vocale</li>
 * </ol>
 * </p>
 *
 * <p>Le {@code ViewPager2} permet à l'utilisateur de naviguer entre ces options
 * par glissement latéral (swipe) configuré dans l'activité hôte ({@code SearchFragment}).</p>
 *
 * <p>Chaque fragment est instancié à la demande par le {@code ViewPager2} et partage
 * un {@link com.example.snaptaplaque.viewmodels.SharedViewModel} scopé à l'activité parente,
 * garantissant la synchronisation des données (saisie par photo, roulette, vocale)
 * entre les différentes options.</p>
 *
 * <h3>Exemple d'utilisation :</h3>
 * <pre>{@code
 * ViewPager2 viewPager = findViewById(R.id.viewPager);
 * ViewPageAdapter adapter = new ViewPageAdapter(this);
 * viewPager.setAdapter(adapter);
 * }</pre>
 *
 * @see FragmentStateAdapter
 * @see PictureFragment
 * @see WheelFragment
 * @see VocalFragment
 * @see com.example.snaptaplaque.viewmodels.SharedViewModel
 */
public class InputSectionAdapter extends FragmentStateAdapter {

    /**
     * Le nombre total d'options (fragments) gérées par cet adaptateur.
     * <p>Correspond aux trois options principales de saisie de la plaque :
     * Photo, Roulette et Vocale.</p>
     */
    private static final int NUM_OPTIONS = 3;

    /**
     * Construit un nouvel adaptateur d'option lié à l'activité hôte spécifiée.
     *
     * <p>L'activité hôte fournit le {@link androidx.fragment.app.FragmentManager}
     * et le {@link androidx.lifecycle.Lifecycle} nécessaires à la gestion
     * du cycle de vie des fragments créés par cet adaptateur.</p>
     *
     * @param searchFragment la {@link SearchFragment} hôte qui héberge le
     *                         {@link androidx.viewpager2.widget.ViewPager2} ;
     *                         ne doit pas être {@code null}
     */
    public InputSectionAdapter(@NonNull SearchFragment searchFragment) {
        super(searchFragment);
    }

    /**
     * Crée et retourne le {@link Fragment} correspondant à la position demandée.
     *
     * <p>La correspondance position → fragment est la suivante :
     * <ul>
     *     <li>{@code 0} → {@link PictureFragment} — permet la saisie ou le scan d'une plaque par photo
     *         d'immatriculation pour ajouter un véhicule à l'historique</li>
     *     <li>{@code 1} → {@link WheelFragment} — permet la saisie ou le scan d'une plaque par roulette
     *         d'immatriculation pour ajouter un véhicule à l'historique</li>
     *     <li>{@code 2} → {@link VocalFragment} — permet la saisie ou le scan d'une plaque par commande vocale
     *         d'immatriculation pour ajouter un véhicule à l'historique</li>
     *     <li>Toute autre valeur → {@link PictureFragment} (fragment par défaut)</li>
     * </ul>
     * </p>
     *
     * <p><strong>Note :</strong> chaque appel crée une nouvelle instance du fragment.
     * Le {@code ViewPager2} gère en interne le recyclage et la restauration
     * des fragments via le {@link FragmentStateAdapter}.</p>
     *
     * @param position la position de l'option dans le {@code ViewPager2} (index basé sur 0,
     *                 valeur attendue entre {@code 0} et {@code 2} inclus)
     * @return une nouvelle instance du {@link Fragment} correspondant à la position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retourne le Fragment correspondant à la position
        switch (position) {
            case 0:
                return new PictureFragment();  // Photo
            case 1:
                return new WheelFragment(); // Roulette
            case 2:
                return new VocalFragment(); // Vocale
            default:
                return new PictureFragment();  // Par défaut
        }
    }

    /**
     * Retourne le nombre total d'options gérées par cet adaptateur.
     *
     * <p>Cette valeur est constante et correspond à {@value #NUM_OPTIONS} fragments :
     * Photo, Roulette et Vocale.</p>
     *
     * @return le nombre de fragments disponibles, soit {@value #NUM_OPTIONS}
     */
    @Override
    public int getItemCount() {
        return NUM_OPTIONS; // Nombre total d'options
    }
}
