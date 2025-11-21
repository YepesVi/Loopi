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
    

    @Query(value = "SELECT * FROM producto p WHERE " +
           
         "(CAST(:titulo AS varchar) IS NULL OR unaccent(p.titulo) ILIKE unaccent(:titulo)) AND " +
           "(:categoriaCount = 0 OR p.categoria_id IN (:categoriaIds)) AND " +
           "(CAST(:precioMin AS double precision) IS NULL OR p.precio >= :precioMin) AND " +
           "(CAST(:precioMax AS double precision) IS NULL OR p.precio <= :precioMax) AND " +
           "(CAST(:estado AS varchar) IS NULL OR p.estado ILIKE :estado) AND " +           
           "(CAST(:propietarioId AS bigint) IS NULL OR p.propietario_id = :propietarioId)",
           
           // --- COUNT QUERY (Debe coincidir exactamente) ---
           countQuery = "SELECT count(p.id) FROM producto p WHERE " +
           "(CAST(:titulo AS varchar) IS NULL OR unaccent(p.titulo) ILIKE unaccent(:titulo)) AND " +
           "(:categoriaCount = 0 OR p.categoria_id IN (:categoriaIds)) AND " +
           "(CAST(:precioMin AS double precision) IS NULL OR p.precio >= :precioMin) AND " +
           "(CAST(:precioMax AS double precision) IS NULL OR p.precio <= :precioMax) AND " +
           "(CAST(:estado AS varchar) IS NULL OR p.estado ILIKE :estado) AND " +
           "(CAST(:propietarioId AS bigint) IS NULL OR p.propietario_id = :propietarioId)",
           nativeQuery = true)
    Page<Producto> buscarConFiltros(
        @Param("titulo") String titulo, 
        @Param("categoriaIds") List<Long> categoriaIds,
        @Param("categoriaCount") int categoriaCount,
        @Param("precioMin") Double precioMin, 
        @Param("precioMax") Double precioMax, 
        @Param("estado") String estado,
        @Param("propietarioId") Long propietarioId,
        Pageable pageable);
    
}
