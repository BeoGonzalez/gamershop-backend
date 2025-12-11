package com.example.gamershop_backend.dto;


public class UsuarioResponse {
    private Long id;
    private String username;
    private String email; // Importante para contacto
    private String rol;

    public UsuarioResponse(Long id, String username, String email, String rol) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
