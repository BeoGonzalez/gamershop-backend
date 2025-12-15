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

    public MyUserDetailService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =================================================================
    // 1. CARGAR USUARIO (LOGIN Y VALIDACIÓN DE TOKEN)
    // =================================================================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscar usuario en la BD
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. --- CHIVATO DE DEPURACIÓN (MIRA ESTO EN LA CONSOLA) ---
        System.out.println("🔎 [MyUserDetailService] Usuario encontrado: " + user.getUsername());
        System.out.println("🔎 [MyUserDetailService] Rol crudo en Base de Datos: '" + user.getRol() + "'");

        // 3. Convertir el Rol de String a Authority
        // Si el rol es null o vacío, asignamos "USER" por defecto para evitar errores
        String rol = (user.getRol() != null && !user.getRol().isEmpty()) ? user.getRol() : "USER";

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(rol));

        System.out.println("🔎 [MyUserDetailService] Autoridad asignada a Spring: " + authorities.get(0).getAuthority());

        // 4. Devolver el objeto User de Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    // =================================================================
    // 2. GUARDAR USUARIO (REGISTRO)
    // =================================================================
    public Usuario saveUser(Usuario usuario) {
        // Encriptar contraseña antes de guardar
        String encodedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encodedPassword);

        // Si no trae rol, poner USER por defecto
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("USER");
        }

        return usuarioRepository.save(usuario);
    }
}