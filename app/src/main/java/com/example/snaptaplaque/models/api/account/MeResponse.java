package com.example.snaptaplaque.models.api.account;

public class MeResponse {
    private Integer id;
    private String email;
    private String username;
    private String full_name;
    private Boolean is_active;
    private Boolean is_admin;
    private String created_at;

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getFull_name() {
        return full_name;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public Boolean getIs_admin() {
        return is_admin;
    }

    public String getCreated_at() {
        return created_at;
    }
}
