package com.example.snaptaplaque.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/*Importation des fragments*/
import com.example.snaptaplaque.fragments.HistoryFragment;
import com.example.snaptaplaque.fragments.ProfileFragment;
import com.example.snaptaplaque.fragments.SearchFragment;

public class ViewPageAdapter extends FragmentStateAdapter {
    // Nombre de pages (Fragments)
    private static final int NUM_PAGES = 3;

    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Retourne le Fragment correspondant à la position
        switch (position) {
            case 0: return new HistoryFragment(); // Historique
            //case 1: return new SearchFragment();  // Recherche
            //case 2: return new ProfileFragment(); // Profil
            default: return new HistoryFragment(); // Par défaut
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES; // Nombre total de pages
    }
}
