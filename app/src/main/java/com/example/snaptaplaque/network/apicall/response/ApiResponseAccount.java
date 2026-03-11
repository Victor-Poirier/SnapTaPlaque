package com.example.snaptaplaque.network.apicall.response;

import com.example.snaptaplaque.models.api.account.DataExportResponse;
import com.example.snaptaplaque.models.api.account.DeleteAccountResponse;
import com.example.snaptaplaque.models.api.account.LoginResponse;
import com.example.snaptaplaque.models.api.account.MeResponse;
import com.example.snaptaplaque.models.api.account.RegisterResponse;

public abstract class ApiResponseAccount {
    public void loginResponse(LoginResponse loginResponse) {

    }

    public void meResponse(MeResponse meResponse) {

    }

    public void registerResponse(RegisterResponse registerResponse) {

    }

    public void dataExportResponse(DataExportResponse dataExportResponse) {

    }

    public void deleteAccount(DeleteAccountResponse deleteAccountResponse){

    }
}