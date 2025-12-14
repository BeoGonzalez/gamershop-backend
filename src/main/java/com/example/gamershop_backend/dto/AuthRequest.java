package com.example.gamershop_backend.dto;

public class AuthRequest {

    private String username;
    private String email;
    private String password;
    private String rol; // Opcional (el backend suele forzar "USER" en registros públicos)

    // 1. Constructor Vacío (OBLIGATORIO para que Spring lea el JSON)
    public AuthRequest() {
    }

    // 2. Constructor Completo (Útil para pruebas o creación manual)
    public AuthRequest(String username, String email, String password, String rol) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // --- Getters y Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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