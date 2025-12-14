package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.AuthRequest;
import com.example.gamershop_backend.dto.AuthResponse;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier lugar (React)
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Inyección por Constructor (Mejor práctica que @Autowired en campos)
    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // ==========================================
    // 1. LOGIN (ADMITE USERNAME O EMAIL)
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        // 1. Validar que venga algo (Usuario o Email)
        String identificador = null;
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            identificador = request.getUsername();
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            identificador = request.getEmail();
        }

        if (identificador == null) {
            return ResponseEntity.badRequest().body("❌ Debes enviar Usuario o Email para entrar.");
        }

        // 2. Buscar Usuario (Inteligente: busca por Username O por Email)
        Optional<Usuario> userOptional = usuarioRepository.findByUsername(identificador);

        if (userOptional.isEmpty()) {
            // Si no lo encuentra por username, intenta por email
            userOptional = usuarioRepository.findByEmail(identificador);
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("❌ El usuario/correo no existe.");
        }

        Usuario usuario = userOptional.get();

        // 3. Verificar contraseña encriptada
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("❌ Contraseña incorrecta.");
        }

        // 4. Generar Token (Usamos el Username siempre como identidad del token)
        String token = jwtUtils.generateToken(usuario.getUsername());

        // 5. Responder con Token, Usuario y Rol
        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getRol() // Esto es vital para que el Frontend sepa si mostrar el AdminPanel
        ));
    }

    // ==========================================
    // 2. REGISTRO (SOLO CLIENTES "USER")
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {

        // Validaciones previas
        if (request.getUsername() == null || request.getPassword() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body("⚠️ Faltan datos obligatorios.");
        }

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ El usuario '" + request.getUsername() + "' ya está ocupado.");
        }
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("⚠️ El email '" + request.getEmail() + "' ya está registrado.");
        }

        // Crear nuevo usuario
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        // IMPORTANTE: Encriptar la contraseña antes de guardar
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Asignamos rol por defecto (Seguridad)
        newUser.setRol("USER");

        usuarioRepository.save(newUser);

        return ResponseEntity.ok("✅ Registro exitoso. ¡Bienvenido a GamerShop!");
    }
}