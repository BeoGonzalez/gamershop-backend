package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.UsuarioResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.service.MyUserDetailService;
import com.example.gamershop_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <--- VITAL
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MyUserDetailService myUserDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. OBTENER MI PERFIL (GET) - Acceso: Authenticated
    // ==========================================
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol()
        ));
    }

    // ==========================================
    // 2. ACTUALIZAR MI PERFIL (PUT) - Acceso: Authenticated
    // ==========================================
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarMiPerfil(@RequestBody Usuario datosNuevos) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return usuarioRepository.findByUsername(username).map(usuario -> {
            if (datosNuevos.getEmail() != null && !datosNuevos.getEmail().isEmpty()) {
                usuario.setEmail(datosNuevos.getEmail());
            }
            if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Perfil actualizado correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 3. LISTAR TODOS (SOLO ADMIN) - Acceso: ADMIN
    // ==========================================
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // <--- BLINDAJE
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRol()))
                .collect(Collectors.toList());
    }

    // ==========================================
    // 4. REGISTRAR USUARIO (POST) - Acceso: Público / Admin
    // ==========================================
    // No ponemos PreAuthorize aquí porque también sirve para el registro público
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(myUserDetailService.saveUser(usuario));
    }

    // ==========================================
    // 5. EDITAR OTRO USUARIO (PUT) - Acceso: ADMIN
    // ==========================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // <--- BLINDAJE
    public ResponseEntity<?> editarUsuarioAdmin(@PathVariable Long id, @RequestBody Usuario datosNuevos) {
        return usuarioRepository.findById(id).map(usuario -> {

            if (datosNuevos.getUsername() != null) usuario.setUsername(datosNuevos.getUsername());
            if (datosNuevos.getEmail() != null) usuario.setEmail(datosNuevos.getEmail());
            if (datosNuevos.getRol() != null) usuario.setRol(datosNuevos.getRol());

            // Solo actualizamos contraseña si envían una nueva
            if (datosNuevos.getPassword() != null && !datosNuevos.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(datosNuevos.getPassword()));
            }

            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Usuario editado por Admin");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // 6. ELIMINAR USUARIO (DELETE) - Acceso: ADMIN
    // ==========================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // <--- BLINDAJE
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}