package com.example.snaptaplaque.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.snaptaplaque.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.snaptaplaque.adapters.ViewPageAdapter;

public class SearchActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

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
