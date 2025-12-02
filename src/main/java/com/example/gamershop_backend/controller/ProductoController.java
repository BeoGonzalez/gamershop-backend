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
@CrossOrigin("*") // Agregado para evitar bloqueos de CORS
public class ProductoController {
    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    // 1. Listar (GET /api/producto)
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    // 2. Obtener por ID (GET /api/producto/{id}) -> FALTABA ESTE
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Guardar / Crear (POST /api/producto)
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        // Al usar 'int' primitivo en el modelo, si no envían stock, Java lo pone en 0 automáticamente.
        // Eliminamos la validación de null ya que int no puede ser null.
        Producto nuevo = repo.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // 4. Actualizar (PUT /api/producto/{id}) -> FALTABA ESTE
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

    // 5. Eliminar (DELETE /api/producto/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 6. PROCESAR COMPRA (POST /api/producto/comprar)
    @PostMapping("/comprar")
    @Transactional
    public ResponseEntity<?> procesarCompra(@RequestBody List<Map<String, Object>> itemsCompra) {
        try {
            for (Map<String, Object> item : itemsCompra) {
                Long id = Long.valueOf(item.get("id").toString());
                int cantidadComprada = Integer.parseInt(item.get("cantidad").toString());

                Producto productoDB = repo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Producto con ID " + id + " no encontrado"));

                // Trabajamos con 'int'. Si tu modelo usa 'Integer' y permite nulos,
                // añade aquí: if (productoDB.getStock() == null) productoDB.setStock(0);
                int stockActual = productoDB.getStock();

                if (stockActual < cantidadComprada) {
                    return ResponseEntity.badRequest()
                            .body("Stock insuficiente para: " + productoDB.getNombre() +
                                    ". Solicitado: " + cantidadComprada +
                                    ", Disponible: " + stockActual);
                }

                productoDB.setStock(stockActual - cantidadComprada);
                repo.save(productoDB);
            }
            return ResponseEntity.ok("Compra procesada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}