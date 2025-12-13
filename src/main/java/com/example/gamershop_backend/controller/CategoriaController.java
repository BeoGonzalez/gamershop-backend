package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Categoria;
import com.example.gamershop_backend.repository.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin("*")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // 1. LISTAR (Ya lo debes tener para que cargue el select)
    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // 2. GUARDAR (ESTE ES EL QUE TE FALTA SEGURO)
    @PostMapping
    public ResponseEntity<Categoria> guardar(@RequestBody Categoria categoria) {
        // Validar que no venga vacío
        if (categoria.getNombre() == null || categoria.getNombre().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Categoria nueva = categoriaRepository.save(categoria);
        return ResponseEntity.ok(nueva);
    }

    // 3. ELIMINAR (Para el botón de borrar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
