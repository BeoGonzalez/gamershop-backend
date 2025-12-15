package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Producto;
import com.example.gamershop_backend.repository.ProductoRepository;
// Importación necesaria
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // PÚBLICO
    @GetMapping
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- AQUÍ APLICAMOS LA SEGURIDAD DIRECTA ---

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    // ==================================================================
    // 🚨 AQUÍ ESTABA EL ERROR (FALTABA setVariantes)
    // ==================================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto productoEditado) {
        return productoRepository.findById(id).map(productoExistente -> {

            productoExistente.setNombre(productoEditado.getNombre());
            productoExistente.setDescripcion(productoEditado.getDescripcion());
            productoExistente.setPrecio(productoEditado.getPrecio());
            productoExistente.setStock(productoEditado.getStock());
            productoExistente.setImagen(productoEditado.getImagen());

            // ✅ AGREGADO: ¡Ahora sí guardamos las variantes!
            productoExistente.setVariantes(productoEditado.getVariantes());

            productoExistente.setCategoria(productoEditado.getCategoria());

            return ResponseEntity.ok(productoRepository.save(productoExistente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!productoRepository.existsById(id)) return ResponseEntity.notFound().build();
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}