package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.UsuarioResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ==========================================
    // 1. LISTAR TODOS LOS USUARIOS
    // ==========================================
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioResponse> usuariosDTO = usuarios.stream()
                .map(u -> new UsuarioResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRol()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usuariosDTO);
    }

    // ==========================================
    // 2. OBTENER MI PERFIL (CAMBIADO A "/perfil")
    // ==========================================
    @GetMapping("/perfil") // <--- AQUÍ ESTÁ EL CAMBIO (Antes era "/me")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil() {
        // Obtenemos el usuario autenticado del contexto de seguridad
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameActual = auth.getName();

        Usuario u = usuarioRepository.findByUsername(usernameActual)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRol()
        ));
    }

    // ==========================================
    // 3. ELIMINAR USUARIO
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}