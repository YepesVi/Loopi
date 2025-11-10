package com.example.demo.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Categoria;
import com.example.demo.repository.CategoriaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    @Transactional
    public Categoria save(Categoria categoria) {
       
        if (categoria.getParent() != null) {
            Long parentId = categoria.getParent().getId();
            if (parentId != null) {
                Optional<Categoria> parentOpt = categoriaRepository.findById(parentId);
                if (parentOpt.isEmpty()) {
                    throw new IllegalArgumentException("El categoría padre con ID " + parentId + " no existe.");
                }
                categoria.setParent(parentOpt.get());
            }
        }
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria update(Long id, Categoria categoriaDetails) {
        
        Categoria categoriaToUpdate = categoriaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría con ID " + id + " no encontrada."));

        Long nuevoParentId = (categoriaDetails.getParent() != null) 
                            ? categoriaDetails.getParent().getId() 
                            : null;
        
        Long currentParentId = (categoriaToUpdate.getParent() != null) 
                                ? categoriaToUpdate.getParent().getId() 
                                : null;

        if (!Objects.equals(nuevoParentId, currentParentId)) {

            Categoria nuevoParent = null;

            if (nuevoParentId != null) {
                
                //VALIDACIÓN CRÍTICA: Existencia del nuevo Padre (Integridad Referencial).
                Optional<Categoria> parentOpt = categoriaRepository.findById(nuevoParentId);
                if (parentOpt.isEmpty()) {
                    throw new IllegalArgumentException("El categoría padre con ID " + nuevoParentId + " no existe.");
                }
                nuevoParent = parentOpt.get();

                //VALIDACIÓN CRÍTICA: Movimiento Seguro (Prevenir ciclos).
                if (!isMovementSafe(id, nuevoParentId)) {
                    throw new IllegalArgumentException(
                        "Movimiento inválido: La categoría no puede ser movida a sí misma ni a uno de sus propios descendientes."
                    );
                }

            }
            categoriaToUpdate.setParent(nuevoParent);
        }
        
        // 4. Actualizar el nombre si se proporciona (asumiendo que las validaciones @NotNull/NotEmpty se hicieron antes).
        if (categoriaDetails.getNombre() != null && !categoriaDetails.getNombre().isEmpty()) {
            categoriaToUpdate.setNombre(categoriaDetails.getNombre());
        }

        return categoriaRepository.save(categoriaToUpdate);
    }

    @Transactional
    public void deleteById(Long id) {
        categoriaRepository.deleteById(id);
    }

    // --- Lógica Específica del Árbol de Categorías ---

    @Transactional(readOnly = true)
    public List<Categoria> findRootCategories() {
        return categoriaRepository.findByParentIsNull();
    }

    @Transactional(readOnly = true)
    public List<Categoria> findChildrenOf(Long parentId) {
        return categoriaRepository.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public List<Categoria> findAllDescendants(Long idAncestro) {
        return categoriaRepository.findAllDescendantsById(idAncestro);
    }

    @Transactional(readOnly = true)
    public List<Long> findAllDescendantIdsWithSelf(Long idAncestro) {
        return categoriaRepository.findAllDescendantIdsWithSelfById(idAncestro);
    }

    @Transactional(readOnly = true)
    public boolean isDescendantOf(Long idAncestro, Long idDescendiente) {
        // Previene la verificación de un nodo como su propio descendiente.
        if (idAncestro.equals(idDescendiente)) {
            return false;
        }
        return categoriaRepository.isDescendantOf(idAncestro, idDescendiente);
    }

    @Transactional(readOnly = true)
    public boolean isMovementSafe(Long categoriaId, Long nuevoParentId) {
        // 1. No se puede mover a sí mismo
        if (categoriaId.equals(nuevoParentId)) {
            return false;
        }

        // 2. No se puede mover a uno de sus propios descendientes
        // Esto previene bucles (ej. Categoria 'A' movida a su nieta 'C')
        if (isDescendantOf(categoriaId, nuevoParentId)) {
            return false;
        }

        return true;
    }
    



}
