package com.example.gamershop_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // Campos de Texto Largo
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio;
    private Integer stock;
    private String imagen;

    @Column(columnDefinition = "TEXT")
    private String variantes; // JSON String: [{"color":"Rojo","url":"..."}]

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // 1. Constructor Vacío (Obligatorio para Hibernate)
    public Producto() {
    }

    // 2. Constructor Completo (ACTUALIZADO)
    public Producto(Long id, String nombre, String descripcion, Double precio, Integer stock, String imagen, String variantes, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.imagen = imagen;
        this.variantes = variantes;
        this.categoria = categoria;
    }

    // 3. Constructor sin ID (Útil para crear nuevos antes de guardar)
    public Producto(String nombre, String descripcion, Double precio, Integer stock, String imagen, String variantes, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.imagen = imagen;
        this.variantes = variantes;
        this.categoria = categoria;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getVariantes() { return variantes; }
    public void setVariantes(String variantes) { this.variantes = variantes; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}