package com.example.gamershop_backend.dto;

public class AuthRequest {
    private String username;
    private String password;
    private String rol;

    // Constructores, Getters y Setters MANUALES
    public AuthRequest() {}

    public AuthRequest(String username, String password, String rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}