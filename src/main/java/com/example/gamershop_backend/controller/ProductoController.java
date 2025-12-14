package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Producto;
import com.example.gamershop_backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*") // Permite que React se conecte sin problemas
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // 1. LISTAR TODOS (Para el catálogo principal)
    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // 2. OBTENER UNO POR ID (¡VITAL! Faltaba esto para el Detalle de Producto)
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREAR NUEVO (Solo Admins)
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // 4. EDITAR COMPLETO (PUT) - Para tu CRUD de Admin
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto productoEditado) {
        return productoRepository.findById(id).map(productoExistente -> {
            // Actualizamos todos los campos
            productoExistente.setNombre(productoEditado.getNombre());
            productoExistente.setDescripcion(productoEditado.getDescripcion());
            productoExistente.setPrecio(productoEditado.getPrecio());
            productoExistente.setStock(productoEditado.getStock());
            productoExistente.setImagen(productoEditado.getImagen());

            // Importante: Actualizar la categoría y las variantes (galería)
            productoExistente.setCategoria(productoEditado.getCategoria());
            productoExistente.setVariantes(productoEditado.getVariantes());

            return ResponseEntity.ok(productoRepository.save(productoExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. ELIMINAR (Solo Admins)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}