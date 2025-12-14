package com.example.gamershop_backend.dto;

public class AuthResponse {

    private String token;
    private String username;
    private String rol;
    private String email; // Agregado para soportar el nuevo AuthController

    // 1. Constructor Vacío (Obligatorio)
    public AuthResponse() {
    }

    // 2. Constructor Completo (El que usa tu AuthController nuevo)
    public AuthResponse(String token, String username, String rol, String email) {
        this.token = token;
        this.username = username;
        this.rol = rol;
        this.email = email;
    }

    // 3. Constructor Antiguo (Por si acaso lo usas en otro lado)
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}