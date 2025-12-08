package com.example.gamershop_backend.dto;

import java.util.List;

public class CompraRequest {

    private String username; // Quién realiza la compra
    private List<ItemCompra> items; // Lista de productos

    // Clase interna estática para mapear cada item del array JSON
    public static class ItemCompra {
        // MODIFICACIÓN: Cambiado de 'productoId' a 'id'
        // para coincidir con el JSON que envía React: { "id": 1, "cantidad": 2 }
        private Long id;
        private Integer cantidad;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }

    // Getters y Setters de la clase principal
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ItemCompra> getItems() {
        return items;
    }

    public void setItems(List<ItemCompra> items) {
        this.items = items;
    }
}