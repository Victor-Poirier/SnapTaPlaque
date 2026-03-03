package com.example.snaptaplaque.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.ViewPageAdapter;

public class SearchActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageView circleHistory;
    private ImageView circleSearch;
    private ImageView circleProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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
