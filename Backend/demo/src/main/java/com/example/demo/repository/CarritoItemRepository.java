package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long>{
    List<CarritoItem> findByCarritoId(Long carritoId); 
    void deleteByProducto_Id(Long productoId);
}
