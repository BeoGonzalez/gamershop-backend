package com.example.gamershop_backend.dto;

import com.example.gamershop_backend.model.Orden;
import java.time.format.DateTimeFormatter;

public class OrdenDTO {
    private Long id;
    private String fecha; // Lo enviaremos como texto formateado
    private Double total;
    private String username;
    private int cantidadItems;

    public OrdenDTO(Orden orden) {
        this.id = orden.getId();
        this.total = orden.getTotal();
        this.username = orden.getUsuario().getUsername();
        this.cantidadItems = orden.getDetalles().size();

        // Formatear la fecha para el gráfico (Día/Mes/Año)
        if (orden.getFecha() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            this.fecha = orden.getFecha().format(formatter);
        } else {
            this.fecha = "Sin fecha";
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getFecha() { return fecha; }
    public Double getTotal() { return total; }
    public String getUsername() { return username; }
    public int getCantidadItems() { return cantidadItems; }
}
