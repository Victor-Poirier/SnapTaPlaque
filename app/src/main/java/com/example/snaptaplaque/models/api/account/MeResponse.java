package com.example.snaptaplaque.models.api.account;

public class MeResponse {
    private Integer id;
    private String email;
    private String username;
    private String full_name;
    private Boolean is_active;
    private Boolean is_admin;
    private String created_at;

    public MeResponse(Integer id, String email, String username, String full_name, Boolean is_active, Boolean is_admin, String created_at) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.full_name = full_name;
        this.is_active = is_active;
        this.is_admin = is_admin;
        this.created_at = created_at;
    }

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
