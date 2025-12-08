package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.CompraRequest;
import com.example.gamershop_backend.model.*;
import com.example.gamershop_backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ordenes")
@CrossOrigin("*")
public class OrdenController {

    private final OrdenRepository ordenRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    // No necesitamos DetalleOrdenRepository explícitamente porque usamos CascadeType.ALL en Orden

    public OrdenController(OrdenRepository ordenRepo, ProductoRepository productoRepo, UsuarioRepository usuarioRepo) {
        this.ordenRepo = ordenRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // 1. GENERAR UNA NUEVA ORDEN (COMPRAR)
    @PostMapping("/comprar")
    @Transactional
    public ResponseEntity<?> crearOrden(@RequestBody CompraRequest request) {
        try {
            // A. Buscar al usuario
            Usuario usuario = usuarioRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // B. Crear la Orden (Cabecera)
            Orden orden = new Orden();
            orden.setUsuario(usuario);
            orden.setEstado("COMPLETADO");

            List<DetalleOrden> detalles = new ArrayList<>();
            double totalOrden = 0.0;

            // C. Procesar cada producto del carrito
            for (CompraRequest.ItemCompra item : request.getItems()) {
                Producto producto = productoRepo.findById(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                // Validar Stock
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
                }

                // Descontar Stock
                producto.setStock(producto.getStock() - item.getCantidad());
                // Si el stock llega a 0, ¿lo borramos o lo dejamos en 0?
                // En un sistema real con historial, NO se borra el producto, se deja en 0 o se desactiva.
                // Si lo borras, perderás la referencia histórica en 'DetalleOrden'.
                // RECOMENDACIÓN: Déjalo en 0.
                productoRepo.save(producto);

                // Crear Detalle
                DetalleOrden detalle = new DetalleOrden();
                detalle.setOrden(orden);
                detalle.setProducto(producto);
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio()); // Precio histórico

                detalles.add(detalle);
                totalOrden += (producto.getPrecio() * item.getCantidad());
            }

            orden.setDetalles(detalles);
            orden.setTotal(totalOrden);

            // D. Guardar todo (Orden + Detalles gracias a Cascade)
            ordenRepo.save(orden);

            return ResponseEntity.ok("Orden #" + orden.getId() + " creada con éxito.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 2. VER MIS COMPRAS
    @GetMapping("/mis-compras/{username}")
    public ResponseEntity<List<Orden>> misCompras(@PathVariable String username) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(ordenRepo.findByUsuarioId(usuario.getId()));
    }
}