package com.example.gamershop_backend.controller;

import com.example.gamershop_backend.dto.AuthRequest;
import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.service.MyUserDetailService;
import com.example.gamershop_backend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final MyUserDetailService userDetailService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, MyUserDetailService userDetailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
    }

    // 1. Endpoint de REGISTRO
    @PostMapping("/registro")
    public ResponseEntity<String> registro(@RequestBody AuthRequest request) {
        // Crear el usuario manualmente
        Usuario newUser = new Usuario();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword()); // Se encriptará en el servicio
        newUser.setRol("USER");

        userDetailService.saveUser(newUser);

        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    // 2. Endpoint de LOGIN
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        try {
            // Esto valida usuario y contraseña (ya encriptada) automáticamente
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }

        // Si la autenticación pasa, generamos el Token
        final String jwt = jwtUtils.generateToken(request.getUsername());
        return ResponseEntity.ok(jwt);
    }
}