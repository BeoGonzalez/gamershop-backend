package com.example.gamershop_backend.service.MyUserDetailService;

import com.example.gamershop_backend.model.Usuario;
import com.example.gamershop_backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos el encoder definido en SecurityConfig

    public MyUserDetailsService(UsuarioRepository userRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    // Lógica para REGISTRAR un usuario
    public Usuario saveUser(Usuario usuario) {
        // 1. Encriptar la contraseña
        String encodedPassword = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(encodedPassword);
        // 2. Guardar en DB
        return usuarioRepository.save(usuario);
    }

    // Lógica para LOGIN (Spring Security la usa automáticamente)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Convertimos nuestra entidad User a un UserDetails de Spring
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // Aquí irían los roles/authorities
        );
    }
}
