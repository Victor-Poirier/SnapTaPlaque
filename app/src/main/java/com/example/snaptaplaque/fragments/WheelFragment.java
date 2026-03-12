package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.adapters.SlotAdapter;

public class WheelFragment extends Fragment {

    private RecyclerView[] slots;
    private Button btnSearch;

    /**
     * Méthode appelée lorsque le fragment est créé.
     *
     * @param inflater Le LayoutInflater utilisé pour inflaté le layout du fragment
     * @param container Le ViewGroup parent du fragment
     * @param savedInstanceState Les données sauvegardées du fragment
     *
     * @return Retourne la vue du fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wheel, container, false);

        // Définition des données
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        // Initialisation des slots
        setupSlot(view.findViewById(R.id.slot1), letters);
        setupSlot(view.findViewById(R.id.slot2), letters);

        setupSlot(view.findViewById(R.id.slot3), numbers);
        setupSlot(view.findViewById(R.id.slot4), numbers);
        setupSlot(view.findViewById(R.id.slot5), numbers);

        setupSlot(view.findViewById(R.id.slot6), letters);
        setupSlot(view.findViewById(R.id.slot7), letters);

        // Récupération des slots
        slots = new RecyclerView[]{
                view.findViewById(R.id.slot1), view.findViewById(R.id.slot2),
                view.findViewById(R.id.slot3), view.findViewById(R.id.slot4), view.findViewById(R.id.slot5),
                view.findViewById(R.id.slot6), view.findViewById(R.id.slot7)
        };

        btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> showToast(getPlateString()));

        return view;
    }

    /**
     * Méthode qui initialise un NumberPicker avec les données fournies.
     *
     * @param recyclerView Le RecyclerView qui contiendra le NumberPicker
     * @param data Les données à afficher dans le NumberPicker
     */
    private void setupSlot(RecyclerView recyclerView, String[] data) {
        if (recyclerView == null) return;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        SlotAdapter adapter = new SlotAdapter(data);
        recyclerView.setAdapter(adapter);

        LinearSnapHelper snapHelper = new LinearSnapHelper();
        // On vérifie s'il n'y a pas déjà un snapHelper attaché
        recyclerView.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(recyclerView);

        // Positionner au milieu pour l'effet infini
        int centerPosition = (Integer.MAX_VALUE / 2);
        centerPosition = centerPosition - (centerPosition % data.length);

        final int finalPosition = centerPosition;

        recyclerView.post(() -> {
            float density = getResources().getDisplayMetrics().density;
            int recyclerViewHeightPx = recyclerView.getHeight();
            int itemHeightPx = (int) (50 * density);
            int offset = (recyclerViewHeightPx - itemHeightPx) / 2;

            layoutManager.scrollToPositionWithOffset(finalPosition, offset);
        });

        // Mise en transparence des items pas au centre
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float midpoint = recyclerView.getHeight() / 2.f;
                    float childMidpoint = (recyclerView.getLayoutManager().getDecoratedTop(child) +
                            recyclerView.getLayoutManager().getDecoratedBottom(child)) / 2.f;
                    float distanceFromCenter = Math.abs(midpoint - childMidpoint);

                    // Plus on est loin du centre, plus c'est transparent (effet 3D)
                    float alpha = 1.0f - Math.min(0.6f, distanceFromCenter / midpoint);
                    child.setAlpha(alpha);
                    child.setScaleX(alpha);
                    child.setScaleY(alpha);
                }
            }
        });

        // Empêcher le parent (ViewPager2 ou autre) d'intercepter le toucher
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Empêche le parent (ViewPager2 ou autre) d'intercepter le toucher
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
    }

    /**
     * Retourne la plaque de la voiture.
     *
     * @return La plaque de la voiture
     */
    private String getPlateString() {
        StringBuilder plate = new StringBuilder();

        for (int i = 0; i < slots.length; i++) {
            RecyclerView rv = slots[i];
            if (rv != null) {
                // On trouve la vue qui est au milieu du RecyclerView
                View centerView = findCenterView(rv);
                if (centerView != null) {
                    TextView txt = centerView.findViewById(R.id.text_item);
                    plate.append(txt.getText().toString());
                }
            }
            // Ajouter les tirets au bon endroit pour le format AA-000-AA
            if (i == 1 || i == 4) {
                plate.append("-");
            }
        }
        return plate.toString();
    }

    /**
     * Méthode qui trouve la vue au centre du RecyclerView.
     *
     * @param recyclerView Le RecyclerView
     * @return La vue au centre du RecyclerView
     */
    private View findCenterView(RecyclerView recyclerView) {
        // Le SnapHelper sait déjà quelle vue est au centre
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        LinearSnapHelper snapHelper = new LinearSnapHelper(); // On peut en créer un temporaire ou réutiliser l'existant
        return snapHelper.findSnapView(layoutManager);
    }

    /**
     *
     */
    private void showToast(String plate) {

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS|WW)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})";

        if(plate.matches(regex_1) || plate.matches(regex_2)) {
            Toast.makeText(getContext(), "La plaque d'immatriculation est valide ✅", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "La plaque d'immatriculation n'est pas valide ❌", Toast.LENGTH_SHORT).show();
        }
    }
}
