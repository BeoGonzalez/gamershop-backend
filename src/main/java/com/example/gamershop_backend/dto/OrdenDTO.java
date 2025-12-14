package com.example.gamershop_backend.dto;

import com.example.gamershop_backend.model.Orden;
import java.time.format.DateTimeFormatter;

public class OrdenDTO {

    private Long id;
    private String fecha;
    private Double total;
    private String username;
    private int cantidadItems;
    private String estado; // <--- ¡VITAL! Faltaba este campo

    // 1. Constructor Vacío (Obligatorio para evitar errores de librerías)
    public OrdenDTO() {
    }

    // 2. Constructor basado en la Entidad (El que usas en el Controller)
    public OrdenDTO(Orden orden) {
        this.id = orden.getId();
        this.total = orden.getTotal();
        this.estado = orden.getEstado(); // Mapeamos el estado

        // Validación de seguridad para Usuario
        if (orden.getUsuario() != null) {
            this.username = orden.getUsuario().getUsername();
        } else {
            this.username = "Usuario Eliminado";
        }

        // Validación de seguridad para Detalles (Evita NullPointerException)
        if (orden.getDetalles() != null) {
            this.cantidadItems = orden.getDetalles().size();
        } else {
            this.cantidadItems = 0;
        }

        // Formatear fecha (Agregué hora y minutos para mejor detalle)
        if (orden.getFecha() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            this.fecha = orden.getFecha().format(formatter);
        } else {
            this.fecha = "Pendiente";
        }
    }

    // --- Getters y Setters (Necesarios para que Spring genere el JSON) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getCantidadItems() { return cantidadItems; }
    public void setCantidadItems(int cantidadItems) { this.cantidadItems = cantidadItems; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}