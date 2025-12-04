package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Producto;
import com.example.gamershop_backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producto")
@CrossOrigin("*") // Permite peticiones desde cualquier origen (útil para desarrollo)
public class ProductoController {
    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // 1. Listar todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    // 2. Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Guardar un producto nuevo
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        // Si el stock viene nulo, lo guardamos como null (permitido por Integer)
        // o puedes forzarlo a 0 aquí si prefieres:
        // if (producto.getStock() == null) producto.setStock(0);

        Producto nuevo = repo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 4. Actualizar producto existente
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto productoDetails) {
        return repo.findById(id)
                .map(producto -> {
                    producto.setNombre(productoDetails.getNombre());
                    producto.setCategoria(productoDetails.getCategoria());
                    producto.setPrecio(productoDetails.getPrecio());
                    producto.setStock(productoDetails.getStock());
                    return ResponseEntity.ok(repo.save(producto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 6. PROCESAR COMPRA (Lógica Defensiva para Stock)
    @PostMapping("/comprar")
    @Transactional // Si falla un producto, se cancela toda la compra (Rollback)
    public ResponseEntity<?> procesarCompra(@RequestBody List<Map<String, Object>> itemsCompra) {
        try {
            for (Map<String, Object> item : itemsCompra) {
                // Obtener datos del JSON
                Long id = Long.valueOf(item.get("id").toString());
                int cantidadComprada = Integer.parseInt(item.get("cantidad").toString());

                // Buscar el producto en la DB
                Producto productoDB = repo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Producto con ID " + id + " no encontrado"));

                // --- MANEJO DEFENSIVO DE INTEGER NULO ---
                // Obtenemos el stock como objeto Integer (puede ser null)
                Integer stockActual = productoDB.getStock();

                // Si es null (producto antiguo o sin stock definido), asumimos que es 0
                if (stockActual == null) {
                    stockActual = 0;
                }

                // Ahora la validación es segura porque stockActual tiene un valor numérico
                if (stockActual < cantidadComprada) {
                    return ResponseEntity.badRequest()
                            .body("Stock insuficiente para: " + productoDB.getNombre() +
                                    ". Solicitado: " + cantidadComprada +
                                    ", Disponible: " + stockActual);
                }

                // Restar y guardar (seguro porque stockActual no es null)
                productoDB.setStock(stockActual - cantidadComprada);
                repo.save(productoDB);
            }

            return ResponseEntity.ok("Compra procesada con éxito. Inventario actualizado.");

        } catch (Exception e) {
            // Si ocurre cualquier error, @Transactional revierte los cambios en la DB
            return ResponseEntity.badRequest().body("Error al procesar la compra: " + e.getMessage());
        }
    }
}