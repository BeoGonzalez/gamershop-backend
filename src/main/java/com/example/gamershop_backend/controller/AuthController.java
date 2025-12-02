package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*") // Permite el acceso desde cualquier origen (necesario para Render/Frontends externos)
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // --- REGISTRO DE USUARIOS ---
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            // 1. Validar si ya existe el usuario
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                // Devolvemos el mensaje que el frontend espera
                return ResponseEntity.badRequest().body("Error: El nombre de usuario ya está en uso.");
            }

            // 2. Encriptar la contraseña (CRUCIAL para que el login funcione)
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // 3. Asignar rol por defecto si no viene en el JSON
            if (usuario.getRol() == null) {
                usuario.setRol(Usuario.Rol.USER);
            }

            // 4. Guardar en BD
            usuarioRepository.save(usuario);

            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el usuario: " + e.getMessage());
        }
    }

    // --- INICIO DE SESIÓN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        try {
            String username = credenciales.get("username");
            String password = credenciales.get("password");

            // 1. Buscar usuario
            Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

            if (usuario == null) {
                // Mensaje genérico de seguridad
                return ResponseEntity.status(401).body("Credenciales incorrectas.");
            }

            // 2. Verificar contraseña encriptada (Esto resuelve el error "Credenciales incorrectas")
            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                // Mensaje genérico de seguridad
                return ResponseEntity.status(401).body("Credenciales incorrectas.");
            }

            // 3. Generar Token JWT (Incluyendo el ROL)
            String token = jwtUtils.generateToken(username, usuario.getRol().name());

            // 4. Responder con todo lo necesario para el Frontend
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    // CORRECCIÓN: Usar .name() para asegurar que el rol es una cadena simple (ADMIN/USER)
                    "rol", usuario.getRol().name(),
                    "username", usuario.getUsername()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el servidor al intentar loguear: " + e.getMessage());
        }
    }
}