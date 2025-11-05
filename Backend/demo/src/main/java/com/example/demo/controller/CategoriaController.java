package com.example.demo.controller;

import com.example.demo.entity.Categoria;
import com.example.demo.service.CategoriaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;


    // --- 1. GET: Obtener Todas las Categorías ---
    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        // Podrías devolver solo las categorías raíz aquí, o todas.
        return ResponseEntity.ok(categoriaService.findAll());
    }
    
    // --- 2. GET: Obtener Categorías Raíz (Nivel Superior) ---
    @GetMapping("/roots")
    public ResponseEntity<List<Categoria>> getRootCategories() {
        return ResponseEntity.ok(categoriaService.findRootCategories());
    }

    // --- 3. GET: Obtener Hijos Directos de una Categoría ---
    @GetMapping("/{id}/children")
    public ResponseEntity<List<Categoria>> getChildren(@PathVariable Long id) {
        // Nota: Esto devolverá una lista vacía si el ID existe pero no tiene hijos.
        return ResponseEntity.ok(categoriaService.findChildrenOf(id));
    }

    // --- 4. GET: Obtener decendientes de una Categoría ---
    @GetMapping("/{id}/descendants")
    public ResponseEntity<List<Categoria>> getDescendants(@PathVariable Long id) {
        // Nota: Esto devolverá una lista vacía si el ID existe pero no tiene hijos.
        return ResponseEntity.ok(categoriaService.findAllDescendants(id));
    }

    // --- 5. GET: Obtener una Categoría por ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Long id) {
        return categoriaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- 6. POST: Crear Nueva Categoría ---
    @PostMapping
    public ResponseEntity<?> createCategoria(@Valid @RequestBody Categoria categoria) {
        try {
            // El Service maneja la validación de existencia del 'parent'
            Categoria createdCategoria = categoriaService.save(categoria);
            return new ResponseEntity<>(createdCategoria, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Captura errores de negocio (ej. "El padre no existe")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 7. PUT: Actualizar Categoría (incluyendo movimiento) ---
    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoriaDetails) {
        try {
            Categoria oldCategoria = categoriaService.findById(id).orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
            // El Service maneja la validación de existencia, del p y del movimiento seguro.
            categoriaDetails.setParent(oldCategoria.getParent());
            Categoria updatedCategoria = categoriaService.update(id, categoriaDetails);
            return ResponseEntity.ok(updatedCategoria);
        } catch (EntityNotFoundException e) {
            // Captura errores si la categoría principal no existe
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // Captura errores de negocio (ej. "El nuevo padre no existe" o "Movimiento inválido")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // --- 7. PUT: Actualizar Categoría (incluyendo movimiento) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategoriaName(@PathVariable Long id, @Valid @RequestBody Categoria categoriaDetails) {
        try {
            // El Service maneja la validación de existencia, del parent, y del movimiento seguro.
            Categoria updatedCategoria = categoriaService.update(id, categoriaDetails);
            return ResponseEntity.ok(updatedCategoria);
        } catch (EntityNotFoundException e) {
            // Captura errores si la categoría principal no existe
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            // Captura errores de negocio (ej. "El nuevo padre no existe" o "Movimiento inválido")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- 8. DELETE: Eliminar Categoría ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        try {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException e) {
            // Opcional: Manejar si intenta borrar algo que no existe
            return ResponseEntity.notFound().build();
        }
    }
    // --- 9. GET: Verificar Descendencia ---
    @GetMapping("/is-descendant")
    public ResponseEntity<Boolean> isDescendant(
            @RequestParam("ancestroId") Long ancestroId,
            @RequestParam("descendienteId") Long descendienteId) {

            if (categoriaService.findById(ancestroId).isEmpty() ||
            categoriaService.findById(descendienteId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }        
        boolean isDescendant = categoriaService.isDescendantOf(ancestroId, descendienteId);
        return ResponseEntity.ok(isDescendant);
    }
}
