package com.example.demo.controller;

import com.example.demo.entity.Producto;
import com.example.demo.service.ProductoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setup() {
        producto = new Producto();
        producto.setId(1L);
        producto.setTitulo("Test Producto");
        producto.setDescripcion("Desc test");
        producto.setEstado("PUBLICADO");
        producto.setPrecio(99.0);
    }

    // -------------------------------------------------------
    // LISTAR TODOS
    // -------------------------------------------------------
    @Test
    void listarTodos_debeRetornar200() throws Exception {
        when(productoService.listarTodos()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Test Producto"));
    }

    // -------------------------------------------------------
    // GET POR ID OK
    // -------------------------------------------------------
    @Test
    void obtenerPorId_ok() throws Exception {
        when(productoService.obtenerPorId(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // -------------------------------------------------------
    // GET POR ID NO EXISTE
    // -------------------------------------------------------
    @Test
    void obtenerPorId_notFound() throws Exception {
        when(productoService.obtenerPorId(1L))
                .thenThrow(new NoSuchElementException("No encontrado"));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // OBTENER PUBLICADOS
    // -------------------------------------------------------
    @Test
    void obtenerPublicados_conContenido() throws Exception {
        when(productoService.obtenerPublicados()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos/publicados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PUBLICADO"));
    }

    @Test
    void obtenerPublicados_sinContenido() throws Exception {
        when(productoService.obtenerPublicados()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos/publicados"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------
    // HISTORIAL PUBLICACIONES
    // -------------------------------------------------------
    @Test
    void historial_conContenido() throws Exception {
        when(productoService.historialPublicaciones(1L, null))
                .thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos/usuario/1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void historial_sinContenido() throws Exception {
        when(productoService.historialPublicaciones(1L, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/productos/usuario/1/historial"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------
    // ELIMINAR PRODUCTO OK
    // -------------------------------------------------------
    @Test
    void eliminar_ok() throws Exception {
        mockMvc.perform(delete("/api/productos/eliminar/1"))
                .andExpect(status().isNoContent());
    }

    // -------------------------------------------------------
    // ELIMINAR PRODUCTO NOT FOUND
    // -------------------------------------------------------
    @Test
    void eliminar_notFound() throws Exception {
        Mockito.doThrow(new NoSuchElementException())
                .when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/productos/eliminar/1"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // ACTUALIZAR ESTADO
    // -------------------------------------------------------
    @Test
    void actualizarEstado_ok() throws Exception {
        when(productoService.actualizarEstadoProducto(eq(1L), eq("VENDIDO")))
                .thenReturn(producto);

        mockMvc.perform(
                put("/api/productos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"VENDIDO\"")
        ).andExpect(status().isOk());
    }

    @Test
    void actualizarEstado_notFound() throws Exception {
        when(productoService.actualizarEstadoProducto(eq(1L), anyString()))
                .thenThrow(new NoSuchElementException());

        mockMvc.perform(
                put("/api/productos/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"VENDIDO\"")
        ).andExpect(status().isNotFound());
    }

    // -------------------------------------------------------
    // BUSCAR CON FILTROS (TEST BÁSICO)
    // -------------------------------------------------------
    @Test
    void buscarConFiltros_ok() throws Exception {
        when(productoService.buscarConFiltros(
                anyString(), anyLong(), anyDouble(),
                anyDouble(), anyString(), anyLong(), any())
        ).thenReturn(new PageImpl<>(List.of(producto), PageRequest.of(0, 10), 1));

        mockMvc.perform(
                get("/api/productos/buscar")
                        .param("titulo", "Test")
                        .param("categoriaId", "1")
                        .param("precioMin", "10")
                        .param("precioMax", "200")
                        .param("estado", "PUBLICADO")
                        .param("propietarioId", "3")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Test Producto"));
    }

    @Test
void crearProductoConImagen_ok() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "imagen.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image bytes".getBytes()
    );

    when(productoService.crearProducto(
            any(Producto.class),
            eq(1L),
            eq(2L),
            anyList()
    )).thenReturn(producto);

    mockMvc.perform(multipart("/api/productos/crear-con-imagen")
                    .file(file)
                    .param("titulo", "Nuevo prod")
                    .param("descripcion", "Desc X")
                    .param("precio", "50")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Test Producto"));
}
@Test
void crearProductoConImagen_categoriaNoExiste() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "imagen.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake".getBytes()
    );

    when(productoService.crearProducto(
            any(Producto.class),
            eq(1L),
            eq(2L),
            anyList()
    )).thenThrow(new NoSuchElementException("Categoría no encontrada"));

    mockMvc.perform(multipart("/api/productos/crear-con-imagen")
                    .file(file)
                    .param("titulo", "Nuevo")
                    .param("descripcion", "Desc")
                    .param("precio", "50")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
            )
            .andExpect(status().isBadRequest());
}


@Test
void crearProductoConImagen_errorIO() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "imagen.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image".getBytes()
    );

    when(productoService.crearProducto(any(), anyLong(), anyLong(), anyList()))
            .thenThrow(new IOException("Error Cloudinary"));

    mockMvc.perform(multipart("/api/productos/crear-con-imagen")
                    .file(file)
                    .param("titulo", "Nuevo")
                    .param("descripcion", "Desc")
                    .param("precio", "50")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
            )
            .andExpect(status().isInternalServerError());
}

@Test
void actualizarProducto_ok() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "img.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "bytes".getBytes()
    );

    when(productoService.actualizarProducto(
            eq(1L),
            any(Producto.class),
            eq(1L),
            eq(2L),
            anyList()
    )).thenReturn(producto);

    mockMvc.perform(multipart("/api/productos/actualizar/1")
                    .file(file)
                    .param("titulo", "Nuevo titulo")
                    .param("descripcion", "Nueva desc")
                    .param("precio", "120")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
                    .with(req -> { req.setMethod("PUT"); return req; }) // 👈 IMPORTANTE
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
}

@Test
void actualizarProducto_notFound() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "img.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "data".getBytes()
    );

    when(productoService.actualizarProducto(anyLong(), any(), anyLong(), anyLong(), anyList()))
            .thenThrow(new NoSuchElementException("No existe"));

    mockMvc.perform(multipart("/api/productos/actualizar/1")
                    .file(file)
                    .param("titulo", "Nuevo")
                    .param("descripcion", "Desc")
                    .param("precio", "50")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
                    .with(req -> { req.setMethod("PUT"); return req; })
            )
            .andExpect(status().isNotFound());
}


@Test
void actualizarProducto_errorIO() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "img.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "data".getBytes()
    );

    when(productoService.actualizarProducto(anyLong(), any(), anyLong(), anyLong(), anyList()))
            .thenThrow(new IOException("Error imágenes"));

    mockMvc.perform(multipart("/api/productos/actualizar/1")
                    .file(file)
                    .param("titulo", "Nuevo")
                    .param("descripcion", "Desc")
                    .param("precio", "50")
                    .param("estado", "PUBLICADO")
                    .param("categoriaId", "1")
                    .param("propietarioId", "2")
                    .with(req -> { req.setMethod("PUT"); return req; })
            )
            .andExpect(status().isInternalServerError());
}

}
