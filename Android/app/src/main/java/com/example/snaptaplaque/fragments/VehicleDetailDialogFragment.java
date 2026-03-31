package com.example.snaptaplaque.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.utils.BrandLogoHelper;
import com.example.snaptaplaque.viewmodels.SharedViewModel;
import com.bumptech.glide.Glide;

/**
 * Fragment de dialogue affichant les caractéristiques détaillées d'un véhicule spécifique.
 *
 * <p>Ce dialogue s'affiche en plein écran avec un arrière-plan transparent. Il récupère
 * l'immatriculation cible via ses arguments, puis interroge la liste des véhicules
 * maintenue dans le {@link SharedViewModel} pour extraire les informations complètes.</p>
 *
 * <p>Les fonctionnalités clés incluent :
 * <ul>
 * <li>Affichage de la plaque, marque, modèle et motorisation.</li>
 * <li>Chargement dynamique du logo du constructeur via {@link BrandLogoHelper} et Glide.</li>
 * <li>Formatage conditionnel des informations techniques additionnelles.</li>
 * </ul>
 * </p>
 *
 * @see DialogFragment
 * @see SharedViewModel
 * @see BrandLogoHelper
 */
public class VehicleDetailDialogFragment extends DialogFragment {

    /**
     * Crée une nouvelle instance du fragment avec l'immatriculation en paramètre.
     *
     * @param immatriculation Le numéro de plaque servant d'identifiant pour la recherche.
     * @return Une instance configurée de {@link VehicleDetailDialogFragment}.
     */
    public static VehicleDetailDialogFragment createFrag(String immatriculation) {
        VehicleDetailDialogFragment fragment = new VehicleDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString("immatriculation", immatriculation);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Configure le thème du dialogue lors de sa création.
     * <p>Utilise un thème sans barre de titre et compatible plein écran.</p>
     *
     * @param savedInstanceState État sauvegardé du fragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    /**
     * Initialise l'interface utilisateur, applique la transparence et lie les données du ViewModel.
     *
     * <p>Le fragment observe la {@code LiveData} des véhicules. Dès que la liste est disponible,
     * il effectue une recherche par immatriculation pour peupler les vues textuelles et
     * déclencher le chargement du logo via Glide.</p>
     *
     * @param inflater           Le {@link LayoutInflater}.
     * @param container          Le conteneur parent.
     * @param savedInstanceState L'état sauvegardé.
     * @return La vue racine {@code dialog_vehicle_detail.xml}.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rend la fenêtre transparente
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_vehicle_detail, container, false);

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Récupère l'immatriculation du véhicule sur lequel on vient de cliquer
        String immatriculation = getArguments().getString("immatriculation");

        System.out.println(immatriculation);

        // Cherche dans la liste le véhicule
        viewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            for(Vehicle v : vehicles) {
                if(v.getImmatriculation().equals(immatriculation)) {
                    // Liaison des vues
                    TextView tvPlate = view.findViewById(R.id.tvPlate);
                    TextView tvBrandModel = view.findViewById(R.id.tvBrandModel);
                    TextView tvEnergy = view.findViewById(R.id.tvEnergy);
                    ImageView imageViewLogo = view.findViewById(R.id.imageViewLogo);

                    // Attribution des valeurs
                    tvPlate.setText(v.getImmatriculation());
                    tvBrandModel.setText(String.format("%s %s", v.getBrand(), v.getModel()));

                    // Formatage optionnel pour les infos et l'énergie
                    String details = v.getEnergy() + (v.getInfo().isEmpty() ? "" : " • " + v.getInfo());
                    tvEnergy.setText(details);

                    // Chargement de l'image du logo du constructeur
                    String logoUrl = BrandLogoHelper.getLogoUrl(v.getBrand());

                    Glide.with(this)
                            .load(logoUrl)
                            .centerInside() // Pour que le logo ne soit pas déformé
                            .placeholder(R.drawable.logo) // Image par défaut pendant le chargement
                            .error(R.drawable.logo)       // Image si le logo n'est pas trouvé
                            .into(imageViewLogo);

                    break;
                }
            }
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}
