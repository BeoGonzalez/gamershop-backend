package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Categoria;
import com.example.gamershop_backend.repository.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*") // Estandarizado para evitar problemas de CORS
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // ==========================================
    // 1. READ (GET) - LISTAR TODAS
    // ==========================================
    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // ==========================================
    // 2. READ (GET) - OBTENER UNA POR ID (Nuevo)
    // ==========================================
    // Vital para precargar el formulario de edición
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable Long id) {
        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 3. CREATE (POST) - GUARDAR NUEVA
    // ==========================================
    @PostMapping
    public ResponseEntity<Categoria> guardar(@RequestBody Categoria categoria) {
        // Validar que no venga vacío
        if (categoria.getNombre() == null || categoria.getNombre().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Categoria nueva = categoriaRepository.save(categoria);
        return ResponseEntity.ok(nueva);
    }

    // ==========================================
    // 4. UPDATE (PUT) - EDITAR EXISTENTE (Nuevo)
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @RequestBody Categoria categoriaEditada) {
        return categoriaRepository.findById(id).map(categoriaExistente -> {

            // Actualizamos solo los datos permitidos
            categoriaExistente.setNombre(categoriaEditada.getNombre());

            // La descripción es opcional, así que actualizamos lo que venga
            categoriaExistente.setDescripcion(categoriaEditada.getDescripcion());

            // Guardamos los cambios
            Categoria actualizada = categoriaRepository.save(categoriaExistente);
            return ResponseEntity.ok(actualizada);

        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 5. DELETE (DELETE) - ELIMINAR
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // OJO: Si la categoría tiene productos asociados, esto podría fallar por SQL Integrity Constraint.
        // Lo ideal sería mover los productos a otra categoría antes de borrar,
        // pero para este nivel básico, el delete directo funciona si la categoría está vacía.
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}