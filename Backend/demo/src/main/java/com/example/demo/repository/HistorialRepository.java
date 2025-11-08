package com.example.demo.repository;

import com.example.demo.entity.Historial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {
    List<Historial> findByProductoIdOrderByFechaRegistroDesc(Long productoId);

    @Query("SELECT h FROM Historial h WHERE h.producto.propietarioId = :propietarioId")
List<Historial> findByPropietarioId(@Param("propietarioId") Long propietarioId);

}

