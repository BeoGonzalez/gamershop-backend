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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordenes")
@CrossOrigin(origins = "*") // Corregido el formato estándar de Spring
public class OrdenController {

    private final OrdenRepository ordenRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public OrdenController(OrdenRepository ordenRepo, ProductoRepository productoRepo, UsuarioRepository usuarioRepo) {
        this.ordenRepo = ordenRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // ==========================================
    // 1. CREATE (POST) - PROCESAR COMPRA
    // ==========================================
    @PostMapping // Ahora responde a POST /ordenes (Estándar REST)
    @Transactional
    public ResponseEntity<?> crearOrden(@RequestBody CompraRequest request) {
        try {
            // Validaciones iniciales
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Usuario requerido.");
            }
            Usuario usuario = usuarioRepo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear Cabecera
            Orden orden = new Orden();
            orden.setUsuario(usuario);
            orden.setEstado("PAGADO"); // Estado inicial por defecto

            List<DetalleOrden> detalles = new ArrayList<>();
            double totalCalculado = 0.0;

            // Procesar Items
            for (CompraRequest.ItemCompra item : request.getItems()) {
                Producto producto = productoRepo.findById(item.getId())
                        .orElseThrow(() -> new RuntimeException("Producto ID " + item.getId() + " no encontrado"));

                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Sin stock para: " + producto.getNombre());
                }

                // DESCONTAR STOCK
                producto.setStock(producto.getStock() - item.getCantidad());
                productoRepo.save(producto); // Guardamos el nuevo stock

                // Crear Detalle
                DetalleOrden detalle = new DetalleOrden(orden, producto, item.getCantidad(), producto.getPrecio());
                detalles.add(detalle);
                totalCalculado += (producto.getPrecio() * item.getCantidad());
            }

            orden.setDetalles(detalles);
            orden.setTotal(totalCalculado);
            ordenRepo.save(orden);

            return ResponseEntity.ok(Map.of("mensaje", "Orden registrada", "id", orden.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. READ (GET) - LISTAR TODAS (ADMIN)
    // ==========================================
    @GetMapping
    public ResponseEntity<List<OrdenDTO>> listarTodas() {
        List<Orden> ordenes = ordenRepo.findAll();
        // Convertimos a DTO para limpiar datos innecesarios y formatear fechas
        List<OrdenDTO> ordenesDTO = ordenes.stream().map(OrdenDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(ordenesDTO);
    }

    // ==========================================
    // 3. READ (GET) - OBTENER UNA POR ID
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return ordenRepo.findById(id)
                .map(orden -> ResponseEntity.ok(new OrdenDTO(orden)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 4. READ (GET) - MIS COMPRAS (USUARIO)
    // ==========================================
    @GetMapping("/mis-compras/{username}")
    public ResponseEntity<List<OrdenDTO>> misCompras(@PathVariable String username) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Orden> ordenes = ordenRepo.findByUsuarioId(usuario.getId());
        List<OrdenDTO> dtos = ordenes.stream().map(OrdenDTO::new).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==========================================
    // 5. UPDATE (PUT) - CAMBIAR ESTADO
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ordenRepo.findById(id).map(orden -> {
            // Buscamos si enviaron un nuevo estado en el JSON {"estado": "ENVIADO"}
            if (body.containsKey("estado")) {
                orden.setEstado(body.get("estado"));
                ordenRepo.save(orden);
                return ResponseEntity.ok("Estado actualizado a: " + body.get("estado"));
            }
            return ResponseEntity.badRequest().body("Falta el campo 'estado'");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 6. DELETE (DELETE) - CANCELAR Y DEVOLVER STOCK
    // ==========================================
    @DeleteMapping("/{id}")
    @Transactional // Vital para asegurar que se devuelva el stock correctamente
    public ResponseEntity<?> eliminarOrden(@PathVariable Long id) {
        return ordenRepo.findById(id).map(orden -> {

            // 1. Devolver el stock de cada producto
            for (DetalleOrden detalle : orden.getDetalles()) {
                Producto p = detalle.getProducto();
                p.setStock(p.getStock() + detalle.getCantidad()); // Sumamos lo que se había vendido
                productoRepo.save(p);
            }

            // 2. Borrar la orden
            ordenRepo.delete(orden);

            return ResponseEntity.ok("Orden eliminada y stock devuelto al inventario.");
        }).orElse(ResponseEntity.notFound().build());
    }
}