package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Categoria;
import com.example.gamershop_backend.model.Producto;
import com.example.gamershop_backend.repository.CategoriaRepository;
import com.example.gamershop_backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@CrossOrigin("*")
public class ProductoController {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;

    public ProductoController(ProductoRepository productoRepo, CategoriaRepository categoriaRepo) {
        this.productoRepo = productoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    // Listar todos
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoRepo.findAll());
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Guardar (Recibe Producto con estructura nested para categoria)
    // El JSON esperado es: { "nombre": "...", "categoria": { "id": 1 }, ... }
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Producto producto) {
        try {
            // Validamos que venga la categoría con ID
            if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
                return ResponseEntity.badRequest().body("La categoría es obligatoria.");
            }

            // Buscamos la categoría real para asegurar que existe y asignarla
            Categoria cat = categoriaRepo.findById(producto.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + producto.getCategoria().getId()));

            producto.setCategoria(cat); // Asignamos el objeto completo gestionado por JPA

            Producto nuevo = productoRepo.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        try {
            Producto producto = productoRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            int nuevoStock = producto.getStock() + cantidad;
            producto.setStock(nuevoStock);

            Producto actualizado = productoRepo.save(producto);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar stock: " + e.getMessage());
        }
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productoRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}