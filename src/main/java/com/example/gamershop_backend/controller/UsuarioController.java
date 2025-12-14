package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.UsuarioResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.service.MyUserDetailService; // Tu servicio
import com.example.gamershop_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*") // Permite que React se conecte
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MyUserDetailService myUserDetailService; // Inyectamos tu servicio para el registro

    // 1. OBTENER MI PERFIL (Para AdminPanel -> ProfileView)
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil() {
        // Obtenemos el usuario del Token actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Retornamos el DTO (sin contraseña)
        return ResponseEntity.ok(new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol()
        ));
    }

    // 2. LISTAR TODOS (Para AdminPanel -> UsersManager)
    @GetMapping
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRol()))
                .collect(Collectors.toList());
    }

    // 3. REGISTRAR ADMIN (Opcional, si quieres crear admins desde Postman/React)
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        // Usamos TU servicio para que encripte la clave antes de guardar
        return ResponseEntity.status(HttpStatus.CREATED).body(myUserDetailService.saveUser(usuario));
    }

    // 4. ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}