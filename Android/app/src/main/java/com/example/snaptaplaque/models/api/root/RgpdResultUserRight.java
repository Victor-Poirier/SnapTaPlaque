package com.example.snaptaplaque.models.api.root;

public class RgpdResultUserRight {
    private String access;
    private String erasure;
    private String rectification;

    public RgpdResultUserRight(String access, String erasure, String rectification) {
        this.access = access;
        this.erasure = erasure;
        this.rectification = rectification;
    }

    public String getAccess() {
        return access;
    }

    public String getErasure() {
        return erasure;
    }

    public String getRectification() {
        return rectification;
    }
}
