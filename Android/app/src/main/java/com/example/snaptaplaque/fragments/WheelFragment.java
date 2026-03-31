package com.example.snaptaplaque.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.adapters.SlotAdapter;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.models.api.vehicles.InfoRequest;
import com.example.snaptaplaque.models.api.vehicles.InfoResponse;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.VehiclesCall;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

import retrofit2.Response;

/**
 * Fragment permettant la saisie d'une plaque d'immatriculation via un système de "roues" rotatives.
 *
 * <p>Ce fragment simule un sélecteur de type machine à sous (Slot Machine) composé de 7 {@link RecyclerView}
 * indépendants. Il gère :
 * <ul>
 * <li>Le défilement infini simulé par un décalage vers le centre de {@link Integer#MAX_VALUE}.</li>
 * <li>Un effet visuel 3D (opacité et échelle) sur les éléments non centrés.</li>
 * <li>L'alignement automatique (Snapping) pour garantir qu'un caractère est toujours sélectionné au centre.</li>
 * </ul>
 * </p>
 *
 * @see com.example.snaptaplaque.adapters.SlotAdapter
 * @see LinearSnapHelper
 */
public class WheelFragment extends Fragment {

    /** Tableau contenant les 7 colonnes de sélection. */
    private RecyclerView[] slots;
    /** Bouton de validation de la saisie. */
    private Button btnSearch;
    /** ViewModel pour l'enregistrement du véhicule trouvé. */
    private SharedViewModel sharedViewModel;

    /**
     * Initialise la vue et configure les 7 slots de sélection.
     * * <p>Définit deux jeux de données (Lettres et Chiffres) et les affecte aux slots
     * selon le format SIV standard : [AA] [000] [AA].</p>
     *
     * @param inflater           Le LayoutInflater.
     * @param container          Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue du fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wheel, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Définition des données
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "V", "W", "X", "Y", "Z"};
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
        btnSearch.setOnClickListener(v -> {
            if(plateComplianceVerification(getPlateString())) {
                getInfoVehicle(new InfoRequest(getPlateString()));
            }
        });

        return view;
    }

    /**
     * Configure un RecyclerView pour se comporter comme une roue de sélection.
     *
     * <p>Cette méthode applique :
     * 1. Un {@link LinearSnapHelper} pour forcer l'arrêt sur un item.
     * 2. Un positionnement initial au milieu de {@link Integer#MAX_VALUE} pour simuler l'infini.
     * 3. Un {@code OnScrollListener} pour l'effet de distorsion visuelle (Alpha/Scale).
     * 4. Une gestion des touchers pour éviter les conflits avec le ViewPager2 parent.</p>
     *
     * @param recyclerView Le RecyclerView à transformer.
     * @param data         Le tableau de chaînes à afficher.
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
            /**
             * Méthode appelée à chaque mouvement de défilement de la roue.
             * * <p>Elle calcule dynamiquement l'apparence de chaque cellule en fonction
             * de sa proximité avec le centre du {@link RecyclerView}.</p>
             *
             * @param recyclerView Le composant en cours de défilement.
             * @param dx           Le déplacement horizontal (0 ici car vertical).
             * @param dy           Le déplacement vertical en pixels.
             */
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
            /**
             * Intercepte l'événement de toucher initial.
             * @param rv Le {@link RecyclerView} concerné.
             * @param e  L'événement de mouvement {@link MotionEvent}.
             * @return {@code false} pour permettre au RecyclerView de traiter normalement le toucher
             * après avoir prévenu le parent.
             */
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

            /**
             * Méthode requise par l'interface, non utilisée ici car le traitement
             * effectif du scroll est géré nativement par le LayoutManager.
             */
            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            /**
             * Appelée lorsque le parent demande de libérer l'interception.
             * @param disallowIntercept État de l'interception.
             */
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    /**
     * Reconstruit la plaque d'immatriculation en lisant l'item central de chaque roue.
     *
     * @return La chaîne formatée (ex: "AB-123-CD").
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
     * Identifie la vue actuellement centrée (sélectionnée) dans une roue.
     *
     * @param recyclerView Le RecyclerView de la roue cible.
     * @return La {@link View} au centre ou {@code null}.
     */
    private View findCenterView(RecyclerView recyclerView) {
        // Le SnapHelper sait déjà quelle vue est au centre
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        LinearSnapHelper snapHelper = new LinearSnapHelper(); // On peut en créer un temporaire ou réutiliser l'existant
        return snapHelper.findSnapView(layoutManager);
    }

    /**
     * Vérifie si la plaque générée correspond aux expressions régulières du format SIV.
     *
     * @param plate La plaque à vérifier.
     * @return {@code true} si valide, sinon affiche un Toast et retourne {@code false}.
     */
    private boolean plateComplianceVerification(String plate) {

        String regex_1 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})-((?!000)[0-9]{3})-((?!SS|WW)[A-HJ-NP-TV-Z]{2})";
        String regex_2 = "(?i)((?!SS|WW|W)[A-HJ-NP-TV-Z]{2})((?!000)[0-9]{3})((?!SS|WW)[A-HJ-NP-TV-Z]{2})";

        if((!plate.matches(regex_1)) && (!plate.matches(regex_2))) {
            Toast.makeText(getContext(), R.string.compliance_plate, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Envoie la requête API pour obtenir les informations du véhicule et affiche le résultat.
     *
     * @param infoRequest Objet de requête contenant la plaque.
     */
    private void getInfoVehicle(InfoRequest infoRequest){
        VehiclesCall.vehicleInfo(infoRequest, new ApiCallback() {
            /**
             * Succès : Ajoute le véhicule au ViewModel et ouvre le dialogue de détails.
             * @param response Réponse API.
             */
            @Override
            public void onResponseSuccess(Response response) {
                InfoResponse res = (InfoResponse) response.body();
                Vehicle vehicle = res.createVehicles(false);

                sharedViewModel.addVehicle(vehicle);

                VehicleDetailDialogFragment dialog = VehicleDetailDialogFragment.createFrag(vehicle.getImmatriculation());
                dialog.show(getChildFragmentManager(), "detail");
            }

            /**
             * Échec : Affiche une erreur et gère l'expiration éventuelle du token.
             * @param response Réponse d'erreur.
             */
            @Override
            public void onResponseFailure(Response response) {
                Toast.makeText(getContext(), R.string.existence_plate, Toast.LENGTH_SHORT).show();
                if ( response.code() == ApiService.ERROR_TOKEN_EXPIRE ){
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }
}