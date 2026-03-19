package com.example.snaptaplaque.models.api.root;

import com.example.snaptaplaque.R;

import java.util.List;

import android.content.Context;

public class RgpdResponse {
    private final String controller;
    private final List<String> contact;
    private final String purpose;
    private final String legal_basis;
    private final List<String> data_collected;
    private final String retention_period;
    private final RgpdResultUserRight user_rights;
    private final String data_sharing;
    private final List<String> security_measures;

    public RgpdResponse(String controller, List<String> contact, String purpose, String legal_basis, List<String> data_collected, String retention_period, RgpdResultUserRight user_rights, String data_sharing, List<String> security_measures) {
        this.controller = controller;
        this.contact = contact;
        this.purpose = purpose;
        this.legal_basis = legal_basis;
        this.data_collected = data_collected;
        this.retention_period = retention_period;
        this.user_rights = user_rights;
        this.data_sharing = data_sharing;
        this.security_measures = security_measures;
    }

    public String createString(Context context) {
        return  context.getString(R.string.rgpd_controller) + controller + "\n\n" +
                context.getString(R.string.rgpd_contact) + "\n" + printContact() + "\n\n" +
                context.getString(R.string.rgpd_purpose) + purpose + "\n\n" +
                context.getString(R.string.rgpd_legal_basis) + legal_basis + "\n\n" +
                context.getString(R.string.rgpd_data_collected) + "\n" + printDataCollected() + "\n\n" +
                context.getString(R.string.rgpd_retention_period) + retention_period + "\n\n" +
                context.getString(R.string.rgpd_user_rights) + user_rights + "\n\n" +
                context.getString(R.string.rgpd_data_sharing) + data_sharing + "\n\n" +
                context.getString(R.string.rgpd_security_measures) + "\n" + printSecurityMeasures() + "\n";
    }

    public String printDataCollected() {
        StringBuilder sb = new StringBuilder();
        for (String data : data_collected) {
            sb.append("\t- ").append(data).append("\n");
        }
        return sb.toString();
    }

    public String printSecurityMeasures() {
        StringBuilder sb = new StringBuilder();
        for (String measure : security_measures) {
            sb.append("\t- ").append(measure).append("\n");
        }
        return sb.toString();
    }

    public String printContact() {
        StringBuilder sb = new StringBuilder();
        for (String contact : contact) {
            sb.append("\t- ").append(contact).append("\n");
        }
        return sb.toString();
    }



    public String getController() {
        return controller;
    }

    public List<String> getContact() {
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
