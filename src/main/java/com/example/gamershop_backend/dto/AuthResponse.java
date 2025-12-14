package com.example.gamershop_backend.dto;

public class AuthResponse {

    private String token;
    private String username;
    private String rol; // Mantengo "rol" para compatibilidad con tu Frontend

    // 1. Constructor Vacío (Estándar obligatorio para frameworks como Jackson/Spring)
    public AuthResponse() {
    }

    // 2. Constructor Completo (El que usa tu AuthController)
    public AuthResponse(String token, String username, String rol) {
        this.token = token;
        this.username = username;
        this.rol = rol;
    }

    // --- Getters y Setters ---

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}