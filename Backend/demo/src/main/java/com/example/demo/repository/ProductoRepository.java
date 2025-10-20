package com.example.demo.repository;

import com.example.demo.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByPropietarioId(Long propietarioId);

    Page<Producto> findByEstadoIgnoreCase(String estado, Pageable pageable);

    Page<Producto> findByPropietarioId(Long propietarioId, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE " +
            "(:titulo IS NULL OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
            "AND (:categoria IS NULL OR p.categoria = :categoria) " +
            "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
            "AND (:precioMax IS NULL OR p.precio <= :precioMax)")
    Page<Producto> buscarConFiltros(@Param("titulo") String titulo,
                                   @Param("categoria") String categoria,
                                   @Param("precioMin") Double precioMin,
                                   @Param("precioMax") Double precioMax,
                                   Pageable pageable);
}
