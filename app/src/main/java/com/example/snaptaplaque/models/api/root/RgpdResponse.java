package com.example.snaptaplaque.models.api.root;

import java.util.List;

public class RgpdResponse {
    private String controller;
    private String contact;
    private String purpose;
    private String legal_basis;
    private List<String> data_collected;
    private String retention_period;
    private RgpdResultUserRight user_rights;
    private String data_sharing;
    private List<String> security_measures;

    public String getController() {
        return controller;
    }

    public String getContact() {
        return contact;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getLegal_basis() {
        return legal_basis;
    }

    public List<String> getData_collected() {
        return data_collected;
    }

    public String getRetention_period() {
        return retention_period;
    }

    public RgpdResultUserRight getUser_rights() {
        return user_rights;
    }

    public String getData_sharing() {
        return data_sharing;
    }

    public List<String> getSecurity_measures() {
        return security_measures;
    }
}
