package com.example.gamershop_backend.dto;

public class AuthRequest {
    private String username;
    private String email; // <--- ¡NUEVO CAMPO NECESARIO!
    private String password;
    private String rol;

    public AuthRequest() {}

    // Constructor actualizado
    public AuthRequest(String username, String email, String password, String rol) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; } // <--- Getter necesario
    public void setEmail(String email) { this.email = email; } // <--- Setter necesario

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}