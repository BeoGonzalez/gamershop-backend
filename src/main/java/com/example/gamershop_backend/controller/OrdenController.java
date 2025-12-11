package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.CompraRequest;
import com.example.gamershop_backend.dto.OrdenDTO;
import com.example.gamershop_backend.model.*;
import com.example.gamershop_backend.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordenes")
@CrossOrigin("*")
public class OrdenController {

    private final OrdenRepository ordenRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public OrdenController(OrdenRepository ordenRepo, ProductoRepository productoRepo, UsuarioRepository usuarioRepo) {
        this.ordenRepo = ordenRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // --- 1. PROCESAR COMPRA (Crear Orden, Detalles y Descontar Stock) ---
    @PostMapping("/comprar")
    @Transactional // Garantiza que si falla algo (ej: sin stock), se cancela TODA la orden
    public ResponseEntity<?> crearOrden(@RequestBody CompraRequest request) {
        try {
            // A. Validar Usuario
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body("Usuario requerido.");
            }
            Usuario usuario = usuarioRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getUsername()));

            // B. Crear Cabecera de la Orden
            Orden orden = new Orden();
            orden.setUsuario(usuario);
            orden.setEstado("COMPLETADO");
            // La fecha se asigna sola en el constructor de Orden (LocalDateTime.now())

            List<DetalleOrden> detalles = new ArrayList<>();
            double totalCalculado = 0.0;

            // C. Recorrer productos del carrito
            for (CompraRequest.ItemCompra item : request.getItems()) {

                // Buscar producto
                Producto producto = productoRepo.findById(item.getId())
                        .orElseThrow(() -> new RuntimeException("Producto ID " + item.getId() + " no encontrado"));

                // Validar Stock
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Disponible: " + producto.getStock());
                }

                // Descontar Stock
                int nuevoStock = producto.getStock() - item.getCantidad();
                producto.setStock(nuevoStock);

                // IMPORTANTE: Guardamos el nuevo stock.
                // NO BORRAMOS el producto (repo.delete) porque rompería la relación con esta Orden en la BD.
                // Si el stock es 0, simplemente no aparecerá disponible en el frontend.
                productoRepo.save(producto);

                // Crear Detalle de Orden (Snapshot de la venta)
                DetalleOrden detalle = new DetalleOrden();
                detalle.setOrden(orden);
                detalle.setProducto(producto);
                detalle.setCantidad(item.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio()); // Guardamos el precio histórico

                detalles.add(detalle);
                totalCalculado += (producto.getPrecio() * item.getCantidad());
            }

            // D. Finalizar y Guardar
            orden.setDetalles(detalles);
            orden.setTotal(totalCalculado);

            // Al guardar la Orden, JPA guarda automáticamente los detalles gracias a CascadeType.ALL
            ordenRepo.save(orden);

            return ResponseEntity.ok("Orden #" + orden.getId() + " registrada con éxito.");

        } catch (Exception e) {
            // Si hay error, @Transactional revierte los cambios de stock y no guarda la orden
            return ResponseEntity.badRequest().body("Error en la compra: " + e.getMessage());
        }
    }

    // --- 2. HISTORIAL DE COMPRAS ---
    @GetMapping("/mis-compras/{username}")
    public ResponseEntity<List<Orden>> misCompras(@PathVariable String username) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(ordenRepo.findByUsuarioId(usuario.getId()));
    }
    @GetMapping
    public ResponseEntity<List<OrdenDTO>> listarTodas() {
        List<Orden> ordenes = ordenRepo.findAll();

        // Convertimos cada Orden a OrdenDTO para que la fecha vaya bonita
        List<OrdenDTO> ordenesDTO = ordenes.stream()
                .map(OrdenDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ordenesDTO);
    }
}