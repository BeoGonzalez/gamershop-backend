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

    // 1. Listar todos los productos (incluyendo su stock actual)
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    // 2. Guardar un producto nuevo (El admin envía el stock inicial aquí)
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        Producto nuevo = repo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 3. Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 4. PROCESAR COMPRA (Lógica de Stock)
    // Recibe una lista de objetos: [{ "id": 1, "cantidad": 2 }, ...]
    @PostMapping("/comprar")
    @Transactional // IMPORTANTE: Si algo falla a mitad de camino, se deshacen todos los cambios (Rollback)
    public ResponseEntity<?> procesarCompra(@RequestBody List<Map<String, Object>> itemsCompra) {
        try {
            for (Map<String, Object> item : itemsCompra) {
                // 1. Obtener datos del JSON
                Long id = Long.valueOf(item.get("id").toString());
                int cantidadComprada = Integer.parseInt(item.get("cantidad").toString());

                // 2. Buscar el producto en la base de datos
                Producto productoDB = repo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Producto con ID " + id + " no encontrado"));

                // 3. Validar si hay suficiente stock
                if (productoDB.getStock() < cantidadComprada) {
                    return ResponseEntity.badRequest()
                            .body("Stock insuficiente para: " + productoDB.getNombre() +
                                    ". Solicitado: " + cantidadComprada +
                                    ", Disponible: " + productoDB.getStock());
                }

                // 4. Restar el stock y guardar
                productoDB.setStock(productoDB.getStock() - cantidadComprada);
                repo.save(productoDB);
            }

            return ResponseEntity.ok("Compra procesada con éxito. Inventario actualizado.");

        } catch (Exception e) {
            // Si ocurre cualquier error (ID no existe, stock insuficiente, error de DB),
            // se devuelve un error 400 y la anotación @Transactional revierte los cambios.
            return ResponseEntity.badRequest().body("Error al procesar la compra: " + e.getMessage());
        }
    }
}