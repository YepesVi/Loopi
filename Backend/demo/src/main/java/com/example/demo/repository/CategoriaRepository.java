package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Método para encontrar todas las categorías de nivel superior (raíces) que tienen parent nulo.
    List<Categoria> findByParentIsNull();

    // Método para encontrar todos los hijos de una categoría específica.
    List<Categoria> findByParentId(Long parentId);

    // --- Consultas Avanzadas para la Jerarquía ---
    // Esta consulta es específica de PostgreSQL (usa la sintaxis WITH RECURSIVE)
    // 
    @Query(value = """
        WITH RECURSIVE categoria_descendientes AS (
            -- Ancla (Selecciona la categoría inicial)
            SELECT id, nombre, parent_id
            FROM categoria
            WHERE id = :idCategoria
            
            UNION ALL
            
            -- Recursión (Busca a los hijos del resultado anterior)
            SELECT c.id, c.nombre, c.parent_id
            FROM categoria c
            INNER JOIN categoria_descendientes cd ON c.parent_id = cd.id
        )
        SELECT * FROM categoria_descendientes
    """, nativeQuery = true)
    List<Categoria> findAllDescendantsById(@Param("idCategoria") Long idCategoria);


    @Query(value = """
        WITH RECURSIVE categoria_descendientes AS (
            -- Ancla (Selecciona la categoría inicial)
            SELECT id
            FROM categoria
            WHERE id = :idCategoria
            
            UNION ALL
            
            -- Recursión (Busca a los hijos del resultado anterior)
            SELECT c.id
            FROM categoria c
            INNER JOIN categoria_descendientes cd ON c.parent_id = cd.id
        )
        -- Selecciona todos los IDs encontrados (incluyendo el padre)
        SELECT id FROM categoria_descendientes
    """, nativeQuery = true)
    List<Long> findAllDescendantIdsWithSelfById(@Param("idCategoria") Long idCategoria);
    
    // Verifica si una categoría es descendiente de otra (hijo, nieto, etc.).
   
    @Query(value = """
        WITH RECURSIVE categoria_descendientes AS (
            -- Ancla (Selecciona la categoría inicial)
            SELECT id
            FROM categoria
            WHERE id = :idAncestro
            
            UNION ALL
            
            -- Recursión (Busca a los hijos del resultado anterior)
            SELECT c.id
            FROM categoria c
            INNER JOIN categoria_descendientes cd ON c.parent_id = cd.id
        )
        SELECT 
            CASE 
                -- Si el ID del descendiente está en el conjunto de descendientes (excluyendo el propio ancestro)
                WHEN EXISTS (
                    SELECT 1 
                    FROM categoria_descendientes 
                    WHERE id = :idDescendiente AND id != :idAncestro
                ) 
                THEN TRUE 
                ELSE FALSE 
            END
    """, nativeQuery = true)
    boolean isDescendantOf(@Param("idAncestro") Long idAncestro, @Param("idDescendiente") Long idDescendiente);

}
