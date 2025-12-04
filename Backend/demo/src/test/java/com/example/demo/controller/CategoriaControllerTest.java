package com.example.demo.controller;

import com.example.demo.entity.Categoria;
import com.example.demo.service.CategoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria categoriaRaiz;
    private Categoria categoriaHija;

    @BeforeEach
    void setUp() {
        categoriaRaiz = new Categoria();
        categoriaRaiz.setId(1L);
        categoriaRaiz.setNombre("Electrónica");

        categoriaHija = new Categoria();
        categoriaHija.setId(2L);
        categoriaHija.setNombre("Celulares");
        categoriaHija.setParent(categoriaRaiz);
    }

    // --- GET: Listar Todas ---
    @Test
    void getAllCategorias_ok() throws Exception {
        when(categoriaService.findAll()).thenReturn(List.of(categoriaRaiz, categoriaHija));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Electrónica"));
    }

    // --- GET: Listar Raíces ---
    @Test
    void getRootCategories_ok() throws Exception {
        when(categoriaService.findRootCategories()).thenReturn(List.of(categoriaRaiz));

        mockMvc.perform(get("/api/categorias/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // --- GET: Por ID ---
    @Test
    void getCategoriaById_ok() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(Optional.of(categoriaRaiz));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    void getCategoriaById_notFound() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST: Crear ---
    @Test
    void createCategoria_ok() throws Exception {
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoriaRaiz);

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRaiz)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    void createCategoria_padreNoExiste_badRequest() throws Exception {
        when(categoriaService.save(any(Categoria.class)))
                .thenThrow(new IllegalArgumentException("El padre no existe"));

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaHija)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT: Actualizar ---
    @Test
    void updateCategoria_ok() throws Exception {
        when(categoriaService.update(eq(1L), any(Categoria.class))).thenReturn(categoriaRaiz);

        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRaiz)))
                .andExpect(status().isOk());
    }

    @Test
void updateCategoria_notFound() throws Exception {
    // 1. Configurar el Mock para lanzar la excepción cuando se llame al servicio
    when(categoriaService.update(eq(99L), any(Categoria.class)))
            .thenThrow(new EntityNotFoundException("No encontrada"));

    mockMvc.perform(put("/api/categorias/99")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\": \"Cualquier Nombre\"}")) 
            .andExpect(status().isNotFound());
}

    @Test
    void updateCategoria_movimientoInvalido_badRequest() throws Exception {
        when(categoriaService.update(eq(1L), any(Categoria.class)))
                .thenThrow(new IllegalArgumentException("Ciclo detectado"));

        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE: Eliminar ---
    @Test
    void deleteCategoria_ok() throws Exception {
        Mockito.doNothing().when(categoriaService).deleteById(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategoria_notFound() throws Exception {
        // En tu controlador usas EmptyResultDataAccessException para detectar 404 en delete
        doThrow(new EmptyResultDataAccessException(1)).when(categoriaService).deleteById(99L);

        mockMvc.perform(delete("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    // --- GET: Descendencia ---
    @Test
    void isDescendant_ok() throws Exception {
        // Mockear que ambos existen
        when(categoriaService.findById(1L)).thenReturn(Optional.of(categoriaRaiz));
        when(categoriaService.findById(2L)).thenReturn(Optional.of(categoriaHija));
        
        when(categoriaService.isDescendantOf(1L, 2L)).thenReturn(true);

        mockMvc.perform(get("/api/categorias/is-descendant")
                .param("ancestroId", "1")
                .param("descendienteId", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}