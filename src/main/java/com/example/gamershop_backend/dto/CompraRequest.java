package com.example.gamershop_backend.dto;

import java.util.List;

public class CompraRequest {
    private String username; // Quién compra
    private List<ItemCompra> items; // Qué compra

    // Clase interna auxiliar para los items
    public static class ItemCompra {
        private Long productoId;
        private Integer cantidad;

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<ItemCompra> getItems() { return items; }
    public void setItems(List<ItemCompra> items) { this.items = items; }
}
