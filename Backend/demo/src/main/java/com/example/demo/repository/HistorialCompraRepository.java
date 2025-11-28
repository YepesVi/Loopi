package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.HistorialCompra;

public interface HistorialCompraRepository extends JpaRepository<HistorialCompra, Long> {
    
}
