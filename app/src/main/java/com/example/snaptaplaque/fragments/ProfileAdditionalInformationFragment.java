package com.example.snaptaplaque.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.snaptaplaque.R;
import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.account.DeleteAccountResponse;
import com.example.snaptaplaque.models.api.model.ModelInfoResponse;
import com.example.snaptaplaque.models.api.root.ApiVersionResponse;
import com.example.snaptaplaque.models.api.root.RgpdResponse;
import com.example.snaptaplaque.network.ApiClient;
import com.example.snaptaplaque.network.ApiService;
import com.example.snaptaplaque.network.apicall.AccountCall;
import com.example.snaptaplaque.network.apicall.ApiCallback;
import com.example.snaptaplaque.network.apicall.ModelCall;
import com.example.snaptaplaque.network.apicall.RootCall;
import com.example.snaptaplaque.network.apicall.response.ApiModelResponse;
import com.example.snaptaplaque.network.apicall.response.ApiResponseAccount;
import com.example.snaptaplaque.network.apicall.response.ApiRootResponse;

public class ProfileAdditionalInformationFragment extends DialogFragment {
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Même singleton Retrofit que dans LaunchActivity
        apiService = ApiClient.getRetrofit().create(ApiService.class);

        // A CHANGER
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        return view;
    }



    public void exportUserData(){
        AccountCall.exportData(new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {

            }

            @Override
            public void onResponseFailure(String message) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, new ApiResponseAccount() {
            @Override
            public void dataExportResponse(DataExportResponse dataExportResponse) {

            }
        });
    }

    public void deleteAccount(){
        AccountCall.deleteAccount(new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {

            }

            @Override
            public void onResponseFailure(String message) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, new ApiResponseAccount() {
            @Override
            public void deleteAccount(DeleteAccountResponse deleteAccountResponse) {

            }
        });
    }

    public void apiVersion(){
        RootCall.apiVersion(new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {

            }

            @Override
            public void onResponseFailure(String message) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, new ApiRootResponse() {
            @Override
            public void apiVersionResponse(ApiVersionResponse apiVersionResponse) {

            }

        });
    }

    public void privacyPolicy(){
        RootCall.privacyPolicy(new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {

            }

            @Override
            public void onResponseFailure(String message) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, new ApiRootResponse() {
            @Override
            public void rgpdResponse(RgpdResponse rgpdResponse) {

            }
        });
    }

    public void modelInfo(){
        ModelCall.modelInfo(new ApiCallback() {
            @Override
            public void onResponseSuccess(String message) {

            }

            @Override
            public void onResponseFailure(String message) {

            }

            @Override
            public void onCallFailure(Throwable t) {

            }
        }, new ApiModelResponse() {
            @Override
            public void modelInfoResponse(ModelInfoResponse modelInfoResponse) {

            }
        });
    }
}
