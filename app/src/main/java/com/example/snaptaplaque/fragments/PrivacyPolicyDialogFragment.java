package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.RootCall;
import retrofit2.Response;

public class PrivacyPolicyDialogFragment extends DialogFragment {

     public static PrivacyPolicyDialogFragment createFrag() {
        return new PrivacyPolicyDialogFragment();
    }

    public PrivacyPolicyDialogFragment() {
        // Constructeur vide requis pour les DialogFragments
    }

     @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Rend la fenêtre transparente
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        View view = inflater.inflate(R.layout.privacy_dialog, container, false);

        RootCall.privacyPolicy(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String privacyText = ((RgpdResponse) response.body()).createString(getContext());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                TextView privacyPolicyRV = view.findViewById(R.id.tvPrivacyContent);
                                privacyPolicyRV.setText(privacyText);
                            });
                        }
                    }
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, getResources().getConfiguration().locale.getLanguage());


        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}
