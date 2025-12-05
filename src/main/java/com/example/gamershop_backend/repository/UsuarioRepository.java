package com.example.gamershop_backend.repository;

import com.example.gamershop_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Este método es mágico en JPA, busca por el campo 'username'
    Usuario findByUsername(String username);
}