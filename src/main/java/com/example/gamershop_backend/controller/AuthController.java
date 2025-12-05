package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.AuthRequest;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import com.example.gamershop_backend.security.JwtUtils;
import com.example.gamershop_backend.service.MyUserDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService userDetailService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, MyUserDetailService userDetailService, UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody AuthRequest request) {
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());

        // Asignación de Rol
        if (request.getRol() != null && !request.getRol().isEmpty()) {
            newUser.setRol(request.getRol());
        } else {
            newUser.setRol("USER");
        }

        userDetailService.saveUser(newUser);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1. Validar Credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        // 2. Generar Token
        final String jwt = jwtUtils.generateToken(request.getUsername());

        // 3. Obtener Usuario Real (CORRECCIÓN OPTIONAL)
        // Usamos .orElseThrow() porque el repositorio devuelve Optional<Usuario>
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Error crítico: Usuario autenticado pero no encontrado en BD"));

        // 4. Armar respuesta JSON
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", usuario.getUsername());

        // Enviar Rol (Evitar null pointer)
        response.put("rol", usuario.getRol() != null ? usuario.getRol() : "USER");

        return ResponseEntity.ok(response);
    }
}