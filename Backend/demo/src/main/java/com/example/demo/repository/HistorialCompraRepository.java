package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.HistorialCompra;

public interface HistorialCompraRepository extends JpaRepository<HistorialCompra, Long> {
    List<HistorialCompra> findByUsuario_CedulaOrderByFechaCompraDesc(String cedula);
}
