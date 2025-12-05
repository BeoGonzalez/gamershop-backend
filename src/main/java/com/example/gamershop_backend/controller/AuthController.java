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
// @CrossOrigin("*") // Descomenta si tienes problemas de CORS locales, pero SecurityConfig ya lo maneja
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService userDetailService;
    private final UsuarioRepository usuarioRepository; // Inyección del repositorio

    // Constructor con todas las inyecciones
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          MyUserDetailService userDetailService,
                          UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
        this.usuarioRepository = usuarioRepository;
    }

    // REGISTRO
    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody AuthRequest request) {
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());

        // Lógica para guardar el ROL correctamente
        if (request.getRol() != null && !request.getRol().isEmpty()) {
            newUser.setRol(request.getRol());
        } else {
            newUser.setRol("USER");
        }

        userDetailService.saveUser(newUser);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    // LOGIN (Versión JSON Corregida)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1. Validar credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        // 2. Generar Token JWT
        final String jwt = jwtUtils.generateToken(request.getUsername());

        // 3. Buscar datos del usuario en la BD (incluyendo el ROL)
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername());

        // 4. Construir respuesta JSON
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", usuario.getUsername());

        // ¡IMPORTANTE! Validamos que el rol no sea nulo para evitar errores
        response.put("rol", usuario.getRol() != null ? usuario.getRol() : "USER");

        return ResponseEntity.ok(response);
    }
}