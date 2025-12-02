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
public class ProductoController {
    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // 1. Listar
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    // 2. Guardar
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        // Al usar 'int' primitivo en el modelo, si no envían stock, Java lo pone en 0 automáticamente.
        // No hace falta verificar null.
        Producto nuevo = repo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 3. Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 4. PROCESAR COMPRA
    @PostMapping("/comprar")
    @Transactional
    public ResponseEntity<?> procesarCompra(@RequestBody List<Map<String, Object>> itemsCompra) {
        try {
            for (Map<String, Object> item : itemsCompra) {
                Long id = Long.valueOf(item.get("id").toString());
                int cantidadComprada = Integer.parseInt(item.get("cantidad").toString());

                Producto productoDB = repo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Producto con ID " + id + " no encontrado"));

                // Trabajamos directamente con int (primitivo)
                int stockActual = productoDB.getStock();

                // Ahora la validación es directa numérica
                if (stockActual < cantidadComprada) {
                    return ResponseEntity.badRequest()
                            .body("Stock insuficiente para: " + productoDB.getNombre() +
                                    ". Solicitado: " + cantidadComprada +
                                    ", Disponible: " + stockActual);
                }

                // Restar y guardar
                productoDB.setStock(stockActual - cantidadComprada);
                repo.save(productoDB);
            }

            return ResponseEntity.ok("Compra procesada con éxito. Inventario actualizado.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la compra: " + e.getMessage());
        }
    }
}