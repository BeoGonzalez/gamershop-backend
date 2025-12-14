package com.example.gamershop_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <--- IMPORTANTE
import jakarta.persistence.*;

@Entity
@Table(name = "detalles_orden")
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la Orden principal
    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @JsonIgnore // <--- ¡ESTO ES VITAL! ROMPE EL BUCLE INFINITO
    private Orden orden;

    // Relación con el Producto comprado
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;

    // Guardamos el precio histórico (cuánto costaba en ese momento)
    @Column(name = "precio_unitario")
    private Double precioUnitario;

    public DetalleOrden() {}

    // Constructor útil para crear detalles rápido
    public DetalleOrden(Orden orden, Producto producto, Integer cantidad, Double precioUnitario) {
        this.orden = orden;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Orden getOrden() { return orden; }
    public void setOrden(Orden orden) { this.orden = orden; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}