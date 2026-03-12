package com.example.snaptaplaque.models.api.root;

public class RgpdResultUserRight {
    private String acces;
    private String erasure;
    private String rectification;

    public RgpdResultUserRight(String acces, String erasure, String rectification) {
        this.acces = acces;
        this.erasure = erasure;
        this.rectification = rectification;
    }

    public String getAcces() {
        return acces;
    }

    public String getErasure() {
        return erasure;
    }

    public String getRectification() {
        return rectification;
    }
}
