package com.example.gamershop_backend.repository;

import com.example.gamershop_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método mágico de JPA para buscar por nombre
    Optional<Usuario> findByUsername(String username);
}