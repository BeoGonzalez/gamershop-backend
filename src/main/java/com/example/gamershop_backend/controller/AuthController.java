package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.AuthRequest;
import com.example.gamershop_backend.dto.AuthResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // ==========================================
    // 1. LOGIN
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String identificador = null;
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            identificador = request.getUsername();
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            identificador = request.getEmail();
        }

        if (identificador == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "❌ Debes enviar Usuario o Email."));
        }

        Optional<Usuario> userOptional = usuarioRepository.findByUsername(identificador);
        if (userOptional.isEmpty()) {
            userOptional = usuarioRepository.findByEmail(identificador);
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "❌ El usuario/correo no existe."));
        }

        Usuario usuario = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "❌ Contraseña incorrecta."));
        }

        String token = jwtUtils.generateToken(usuario.getUsername());

        // Devolvemos el AuthResponse con datos clave
        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getEmail() // Agrega esto a tu AuthResponse si quieres guardarlo en el front
        ));
    }

    // ==========================================
    // 2. REGISTRO
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {

        // 1. Validaciones básicas
        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "⚠️ Faltan datos obligatorios."));
        }

        // 2. Validar duplicados
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "⚠️ El usuario '" + request.getUsername() + "' ya existe."));
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "⚠️ El email '" + request.getEmail() + "' ya está registrado."));
        }

        // 3. Crear usuario
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRol("USER"); // Rol por defecto

        usuarioRepository.save(newUser);

        // 4. Respuesta JSON limpia
        return ResponseEntity.ok(Map.of("message", "✅ Registro exitoso. ¡Bienvenido a GamerShop!"));
    }
}