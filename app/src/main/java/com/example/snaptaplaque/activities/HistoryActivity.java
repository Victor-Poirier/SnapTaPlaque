package com.example.snaptaplaque.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.snaptaplaque.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.snaptaplaque.adapters.ViewPageAdapter;

public class HistoryActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);

        // Configuration de l'adaptateur du ViewPager (Fragments pour chaque page)
        ViewPageAdapter adapter = new ViewPageAdapter(this);
        viewPager.setAdapter(adapter);

        // Sync : Clic sur menu -> Change la page
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) viewPager.setCurrentItem(0);
            else if (id == R.id.nav_search) viewPager.setCurrentItem(1);
            else if (id == R.id.nav_profile) viewPager.setCurrentItem(2);
            return true;
        });

        // Sync : Slide de la page -> Change l'icône active en bas
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNav.getMenu().getItem(position).setChecked(true);
            }
        });
    }
}
