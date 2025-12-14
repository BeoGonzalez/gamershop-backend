package com.example.gamershop_backend.service;

import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Corregí el constructor (tenías el repositorio duplicado)
    public MyUserDetailService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lógica para REGISTRAR
    public Usuario saveUser(Usuario usuario) {
        String encodedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encodedPassword);

        // Aseguramos que tenga un rol por defecto si viene nulo
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("USER");
        }

        return usuarioRepository.save(usuario);
    }

    // Lógica para LOGIN
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // --- CORRECCIÓN CRÍTICA AQUÍ ---
        // Convertimos el String "ADMIN" de la BD en una autoridad real de Spring
        List<SimpleGrantedAuthority> authorities;

        if (user.getRol() != null && !user.getRol().isEmpty()) {
            authorities = List.of(new SimpleGrantedAuthority(user.getRol()));
        } else {
            authorities = List.of(new SimpleGrantedAuthority("USER")); // Rol por defecto
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities // <--- ¡AQUÍ PASAMOS EL ROL REAL!
        );
    }
}