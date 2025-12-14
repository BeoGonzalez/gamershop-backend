package com.example.gamershop_backend.dto;

import java.util.List;

public class CompraRequest {

    private String username;        // Usuario que compra
    private List<ItemCompra> items; // Array de productos: [{"id": 1, "cantidad": 2}, ...]

    // 1. Constructor Vacío (OBLIGATORIO para Spring/Jackson)
    public CompraRequest() {
    }

    // 2. Constructor Completo (Opcional, útil para tests)
    public CompraRequest(String username, List<ItemCompra> items) {
        this.username = username;
        this.items = items;
    }

    // --- CLASE INTERNA (DTO para cada item) ---
    public static class ItemCompra {
        private Long id;        // Coincide con el { id: ... } del JSON de React
        private Integer cantidad;

        // Constructor Vacío (VITAL para la clase interna)
        public ItemCompra() {
        }

        public ItemCompra(Long id, Integer cantidad) {
            this.id = id;
            this.cantidad = cantidad;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    // --- Getters y Setters Principales ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<ItemCompra> getItems() { return items; }
    public void setItems(List<ItemCompra> items) { this.items = items; }
}