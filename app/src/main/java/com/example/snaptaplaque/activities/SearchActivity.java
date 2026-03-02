package com.example.snaptaplaque.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.views.DashBoardGauge;
import com.example.snaptaplaque.adapters.ViewPageAdapter;

public class SearchActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private DashBoardGauge gaugeHistory;
    private DashBoardGauge gaugeSearch;
    private DashBoardGauge gaugeProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        viewPager = findViewById(R.id.viewPager);
        gaugeHistory = findViewById(R.id.gaugeHistory);
        gaugeSearch = findViewById(R.id.gaugeSearch);
        gaugeProfile = findViewById(R.id.gaugeProfile);

        if (viewPager == null || gaugeHistory == null || gaugeSearch == null || gaugeProfile == null)
            return;

        gaugeHistory.setIconResId(R.drawable.ic_history);
        gaugeSearch.setIconResId(R.drawable.ic_search);
        gaugeProfile.setIconResId(R.drawable.ic_profile);

        // Configuration de l'adaptateur
        ViewPageAdapter adapter = new ViewPageAdapter(this);
        viewPager.setAdapter(adapter);

        // Clicks sur les gauges
        gaugeHistory.setOnClickListener(v -> selectTab(0));
        gaugeSearch.setOnClickListener(v -> selectTab(1));
        gaugeProfile.setOnClickListener(v -> selectTab(2));

        // Synchronisation ViewPager → Gauges
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateGaugeSelection(position);
            }
        });

        // Positionnement initial sur "Search" (index 1)
        viewPager.setCurrentItem(1, false);
        updateGaugeSelection(1);
    }

    // Change de page
    private void selectTab(int index) {
        viewPager.setCurrentItem(index, true);
        updateGaugeSelection(index);
    }

    // Met à jour visuellement les gauges
    private void updateGaugeSelection(int selectedIndex) {

        gaugeHistory.setSelectedGauge(selectedIndex == 0);
        gaugeSearch.setSelectedGauge(selectedIndex == 1);
        gaugeProfile.setSelectedGauge(selectedIndex == 2);

        gaugeHistory.setTabIndex(selectedIndex);
        gaugeSearch.setTabIndex(selectedIndex);
        gaugeProfile.setTabIndex(selectedIndex);
    }
}
