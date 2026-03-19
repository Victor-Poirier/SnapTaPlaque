package com.example.snaptaplaque.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.activities.SignInActivity;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.ModelCall;
import com.example.snaptaplaque.network.apicall.RootCall;

import retrofit2.Response;

public class ProfileAdditionalInformationFragment extends DialogFragment {
    private ApiService apiService;

    private TextView apiVersion;
    private TextView privacyPolicy;

    private TextView modelInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Même singleton Retrofit que dans LaunchActivity
        apiService = ApiClient.getRetrofit().create(ApiService.class);

        // A CHANGER
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        privacyPolicy();
        modelInfo();

        return view;
    }

    public void exportUserData(){
        AccountCall.exportData(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {

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

    public void deleteAccount(){
        AccountCall.deleteAccount(new ApiCallback() {
            @Override
            public void onResponseSuccess(Response response) {

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

                modelInfo.setText(res.createString());
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