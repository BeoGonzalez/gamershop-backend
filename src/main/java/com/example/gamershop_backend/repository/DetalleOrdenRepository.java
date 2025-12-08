package com.example.gamershop_backend.repository;

import com.example.gamershop_backend.model.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {
    // Por ahora no necesitamos métodos especiales, JpaRepository ya trae todo.
}
