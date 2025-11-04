package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar por correo (login o recuperación)
    Optional<User> findByCorreo(String correo);

    // Buscar por cédula (para edición de perfil)
    Optional<User> findByCedula(String cedula);

    // Validar si ya existe una cédula registrada
    boolean existsByCedula(String cedula);

    // Validar si ya existe un correo registrado
    boolean existsByCorreo(String correo);
}
