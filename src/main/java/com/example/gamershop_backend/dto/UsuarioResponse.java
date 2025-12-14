package com.example.gamershop_backend.dto;

public class UsuarioResponse {

    private Long id;
    private String username;
    private String email;
    private String rol;

    // 1. Constructor Vacío (OBLIGATORIO para el estándar Java Bean/Jackson)
    public UsuarioResponse() {
    }

    // 2. Constructor Completo (El que usas en el Controller para mapear)
    public UsuarioResponse(Long id, String username, String email, String rol) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}