package com.example.snaptaplaque.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.activities.SignUpActivity;
import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.ModelCall;
import com.example.snaptaplaque.network.apicall.RootCall;
import com.example.snaptaplaque.utils.SessionManager;

import retrofit2.Response;

public class ProfileAdditionalInformationFragment extends DialogFragment {
    private ApiService apiService;
    private Button bn_export_data;
    private TextView tv_export_data_response;
    private TextView tv_api_version;
    private TextView tv_model_info;
    private Button bn_delete_account;
    private Button bn_close;
    SessionManager sessionManager;

    public static ProfileAdditionalInformationFragment createFrag() {
        ProfileAdditionalInformationFragment fragment = new ProfileAdditionalInformationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        sessionManager = new SessionManager(getContext());

        apiService = ApiClient.getRetrofit().create(ApiService.class);
        View view = inflater.inflate(R.layout.fragment_info_api, container, false);

        bn_export_data = view.findViewById(R.id.btnDataExport);
        tv_export_data_response = view.findViewById(R.id.tvDataExportResponse);
        tv_api_version = view.findViewById(R.id.tvApiVersionResponse);
        tv_model_info = view.findViewById(R.id.tvModelInfoResponse);
        bn_delete_account = view.findViewById(R.id.btnDeleteAccount);
        bn_close = view.findViewById(R.id.btnClose);

        bn_export_data.setOnClickListener(v->{
            exportUserData();
        });

        bn_delete_account.setOnClickListener(v->{
            deleteAccount();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            sessionManager.logout();
            Toast.makeText(getContext(), R.string.delete_account_successful, Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        bn_close.setOnClickListener(v -> dismiss());

        modelInfo();
        apiVersion();

        return view;
    }

    public void exportUserData() {
        AccountCall.exportData(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                DataExportResponse res = (DataExportResponse) response.body();

                if (res != null && getContext() != null) {
                    try {
                        String jsonString = new com.google.gson.Gson().toJson(res);

                        // define the path: Android/data/com.package.../files/Documents/
                        java.io.File directory = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                        if (directory == null) {
                            // Fallback if external storage is unavailable
                            directory = requireContext().getFilesDir();
                        }

                        java.io.File file = new java.io.File(directory, "user_data_export.json");

                        java.io.FileWriter writer = new java.io.FileWriter(file);
                        writer.write(jsonString);
                        writer.flush();
                        writer.close();

                        String successMessage = "File saved at:\n" + file.getAbsolutePath();
                        tv_export_data_response.setText(successMessage);

                    } catch (java.io.IOException e) {
                        tv_export_data_response.setText("Error: Saving data isn't possible currently");
                    }
                } else {
                    tv_export_data_response.setText("Error: No data available to export.");
                }
            }

            @Override
            public void onResponseFailure(Response response) {
                if (response.code() == ApiService.ERROR_TOKEN_EXPIRE) {
                    Intent intent = new Intent(getActivity(), SignInActivity.class);
                    if (getActivity() != null) {
                        getActivity().startActivity(intent);
                    }
                } else {
                    tv_export_data_response.setText("API Error: " + response.code());
                }
            }

            @Override
            public void onCallFailure(Throwable t) {
                Log.e("ProfileInfo", t.getMessage());
            }
        });
    }


    public void deleteAccount(){
        AccountCall.deleteAccount(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                getActivity().startActivity(intent);
            }

            @Override
            public void onResponseFailure(Response response) {
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

    public void apiVersion(){
        RootCall.apiVersion(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {

            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }

    public void privacyPolicy(){
        RootCall.privacyPolicy(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                RgpdResponse res = (RgpdResponse) response.body();
                privacyPolicy.setText(res.createString(getContext()));
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, getContext().getResources().getConfiguration().locale.getLanguage());
    }

    public void modelInfo(){
        ModelCall.modelInfo(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {
                ModelInfoResponse res = (ModelInfoResponse) response.body();
                tv_model_info.setText(res.createString(getContext()));
            }

            @Override
            public void onResponseFailure(Response response) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        });
    }
}