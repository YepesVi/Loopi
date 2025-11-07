package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByEstadoIgnoreCase(String estado, Pageable pageable);

    List<Producto> findByEstadoIgnoreCase(String estado);

    Page<Producto> findByPropietarioId(Long propietarioId, Pageable pageable);

    List<Producto> findByPropietarioId(Long propietarioId);
    

    @Query("SELECT p FROM Producto p WHERE " +
           "(:titulo IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:categoriaIds IS NULL OR p.categoria.id IN :categoriaIds) AND " + 
           "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
           "(:precioMax IS NULL OR p.precio <= :precioMax)")
    Page<Producto> buscarConFiltros(
        @Param("titulo") String titulo, 
        @Param("categoriaIds") List<Long> categoriaIds,
        @Param("precioMin") Double precioMin, 
        @Param("precioMax") Double precioMax, 
        Pageable pageable);
    
}
