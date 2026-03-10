package com.example.snaptaplaque.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.Vehicle;
import com.example.snaptaplaque.viewmodels.SharedViewModel;

public class VehicleDetailDialogFragment extends DialogFragment {

    public static VehicleDetailDialogFragment newInstance(String detail) {
        VehicleDetailDialogFragment fragment = new VehicleDetailDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("detail", detail);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rend la fenêtre transparente
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = inflater.inflate(R.layout.dialog_vehicle_detail, container, false);

        String detail = getArguments().getString("detail");
        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Cherche dans la liste le véhicule
        viewModel.getVehicleList().observe(getViewLifecycleOwner(), vehicles -> {
            for(Vehicle v : vehicles) {
                if(v.getDetails().equals(detail)) {
                    ((TextView) view.findViewById(R.id.tvDetail)).setText(v.getDetails());
                    break;
                }
            }
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}
