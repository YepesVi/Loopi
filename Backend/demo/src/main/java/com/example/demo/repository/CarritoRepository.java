package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long>{
    Optional<Carrito> findByUser_Cedula(String usuarioCedula);
} 
