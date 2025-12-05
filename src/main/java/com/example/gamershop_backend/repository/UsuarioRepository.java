package com.example.gamershop_backend.repository;

import com.example.gamershop_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // <--- Importamos Optional

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // CORRECCIÓN: Cambiamos 'Usuario' por 'Optional<Usuario>'
    // Esto permite usar .orElseThrow() en el servicio sin errores.
    Optional<Usuario> findByUsername(String username);
}