package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.AuthRequest;
import com.example.gamershop_backend.dto.AuthResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // ==========================================
    // 1. LOGIN CON EMAIL
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        // Validar que venga el email
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ El email es obligatorio para iniciar sesión.");
        }

        // Buscar por EMAIL
        Optional<Usuario> userOptional = usuarioRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("❌ El correo no está registrado.");
        }

        Usuario usuario = userOptional.get();

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("❌ Contraseña incorrecta.");
        }

        // Generar Token
        String token = jwtUtils.generateToken(usuario.getUsername());

        // Responder
        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getRol() // CAMBIO: Ya es String, no necesita .toString()
        ));
    }

    // ==========================================
    // 2. REGISTRO (SOLO CLIENTES "USER")
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {

        // Validaciones básicas
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ El usuario '" + request.getUsername() + "' ya existe.");
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ El email '" + request.getEmail() + "' ya está registrado.");
        }

        // Crear usuario
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // CAMBIO IMPORTANTE: Asignamos el rol como String directo
        newUser.setRol("USER");

        usuarioRepository.save(newUser);

        return ResponseEntity.ok("✅ Usuario registrado con éxito. Ahora inicia sesión.");
    }
}