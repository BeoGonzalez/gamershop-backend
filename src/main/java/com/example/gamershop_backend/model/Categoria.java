package com.example.gamershop_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- Importante
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Relación inversa: Una categoría tiene muchos productos.
    // Usamos @JsonIgnore para que al pedir la lista de categorías,
    // NO se traiga todos los productos (evita lentitud y bucles).
    @OneToMany(mappedBy = "categoria")
    @JsonIgnore // <--- ¡LA CLAVE DE LA VELOCIDAD!
    private List<Producto> productos;

    public Categoria() {
    }

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}