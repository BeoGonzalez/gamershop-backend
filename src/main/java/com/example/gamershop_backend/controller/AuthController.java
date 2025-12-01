package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }
        // Encriptamos la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Aseguramos que el rol venga bien, si no, por defecto USER
        if (usuario.getRol() == null) {
            usuario.setRol(Usuario.Rol.USER);
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String username = credenciales.get("username");
        String password = credenciales.get("password");

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            // Generamos token incluyendo el ROL
            String token = jwtUtils.generateToken(username, usuario.getRol().name());

            // Devolvemos Token y Rol para que React sepa qué mostrar
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "rol", usuario.getRol(),
                    "username", usuario.getUsername()
            ));
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }
}
