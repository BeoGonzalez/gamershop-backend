package com.example.gamershop_backend.model;

import jakarta.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String categoria;
    private int precio;
    private Integer stock;

    public Producto() {
    }

    public Producto(Long id, String nombre, String categoria, int precio, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }

    // Getters y Setters de Stock
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}