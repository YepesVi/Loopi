package com.example.demo;

import com.example.demo.controller.ProductoController;
import com.example.demo.entity.Categoria;
import com.example.demo.entity.Producto;
import com.example.demo.service.CategoriaService; // 🌟 IMPORTAR SERVICIO
import com.example.demo.service.ProductoService; // 🌟 IMPORTAR SERVICIO
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile; 
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class) // Testea el Controller
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private CategoriaService categoriaService; // Necesario porque ProductoService depende de él

    @Autowired
    private ObjectMapper objectMapper;

    private Producto productoMock;
    private Categoria categoriaMock;
    private MockMultipartFile fileMock;

    @BeforeEach
    void setUp() {
        categoriaMock = new Categoria(); //
        categoriaMock.setId(1L);
        categoriaMock.setNombre("Electrónica");

        productoMock = new Producto(); //
        productoMock.setId(1L);
        productoMock.setTitulo("Producto Test");
        productoMock.setDescripcion("Descripción de prueba");
        productoMock.setCategoria(categoriaMock); 
        productoMock.setPrecio(10000.0);
        productoMock.setEstado("publicado");
        //productoMock.setFotos("/uploads/foto.jpg");
        //productoMock.setPropietarioId(1L);
        productoMock.setFechaPublicacion(LocalDateTime.now());

        // Archivo simulado
        fileMock = new MockMultipartFile(
                "file", // Nombre del @RequestParam
                "foto_test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test-image-content".getBytes()
        );
    }

    // --- Tests de Endpoints GET ---

    @Test
    public void listarTodos_retornaOkYListaDeProductos() throws Exception {
        when(productoService.listarTodos()).thenReturn(List.of(productoMock));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].titulo").value("Producto Test"));
    }

    @Test
    public void obtenerPorId_existe_retornaOk() throws Exception {
        when(productoService.obtenerPorId(1L)).thenReturn(productoMock);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void obtenerPorId_noExiste_retornaNotFound() throws Exception {
        when(productoService.obtenerPorId(99L)).thenThrow(new NoSuchElementException("No encontrado"));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void obtenerPublicados_retornaOk() throws Exception {
        when(productoService.obtenerPublicados()).thenReturn(List.of(productoMock));

        mockMvc.perform(get("/api/productos/publicados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("publicado"));
    }

    @Test
    public void historialPublicaciones_retornaOk() throws Exception {
        Long propietarioId = 1L;
        when(productoService.historialPublicaciones(eq(propietarioId), any())).thenReturn(List.of(productoMock));

        mockMvc.perform(get("/api/productos/usuario/{propietarioId}/historial", propietarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Producto Test"));
    }

    @Test
    public void buscarProductos_conFiltros_retornaResultados() throws Exception {
        Page<Producto> pagina = new PageImpl<>(List.of(productoMock));

        // Mockear el SERVICIO para que acepte Long en categoriaId
        when(productoService.buscarConFiltros(
            anyString(), // titulo
            eq(1L),      // Probar que se envía el Long
            anyDouble(), // precioMin
            anyDouble(), // precioMax
            anyString(), // estado
            eq(1L),      // propietarioId
            any(Pageable.class)
        )).thenReturn(pagina);

        mockMvc.perform(get("/api/productos/buscar")
                .param("titulo", "Producto")
                .param("categoriaId", "1")
                .param("precioMin", "1000")
                .param("precioMax", "2500")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].titulo").value("Producto Test"));
    }

    // --- Tests de Endpoints POST ---

    @Test
    public void crearProducto_valido_retornaCreated() throws Exception {
        // Mockear el servicio
       // when(productoService.crearProducto(any(Producto.class), eq(1L), anyList()))
       //     .thenReturn(productoMock);

        mockMvc.perform(multipart("/api/productos/crear-con-imagen") // 👈 Usar multipart
                .file(fileMock) // Adjuntar archivo
                .param("titulo", "Producto Test")
                .param("descripcion", "Descripción de prueba")
                .param("categoriaId", "1") 
                .param("precio", "10000.0")
                .param("estado", "publicado")
                .param("propietarioId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated()) 
            .andExpect(jsonPath("$.titulo").value("Producto Test"));
    }

    @Test
    public void crearProducto_categoriaNoExiste_retornaBadRequest() throws Exception {
        // Mockear el servicio para que falle al buscar la categoría
      //  when(productoService.crearProducto(any(Producto.class), eq(99L), anyList()))
       //     .thenThrow(new NoSuchElementException("Categoría no encontrada"));

        mockMvc.perform(multipart("/api/productos/crear-con-imagen")
                .file(fileMock)
                .param("titulo", "Test")
                .param("categoriaId", "99") // ID de categoría que no existe
                
                .param("descripcion", "d")
                .param("precio", "1")
                .param("estado", "e")
                .param("propietarioId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest()); // Esperar 400
    }

    @Test
    public void crearProducto_errorDeIOException_retornaInternalServerError() throws Exception {
        // Mockear el servicio para que falle al guardar la imagen
    //    when(productoService.crearProducto(any(Producto.class), eq(1L), anyList()))
     //       .thenThrow(new IOException("Error al guardar"));

        mockMvc.perform(multipart("/api/productos/crear-con-imagen")
                .file(fileMock)
                .param("titulo", "Test")
                .param("categoriaId", "1")
                // ... otros params
                 .param("descripcion", "d")
                .param("precio", "1")
                .param("estado", "e")
                .param("propietarioId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isInternalServerError()); // Esperar 500
    }


    // --- Tests de Endpoints PUT ---

    @Test
    public void actualizarProducto_valido_retornaOk() throws Exception {
//        when(productoService.actualizarProducto(eq(1L), any(Producto.class), eq(1L), anyList()))
 //           .thenReturn(productoMock);

        mockMvc.perform(multipart("/api/productos/actualizar/1")
                .file(fileMock) // Enviar un nuevo archivo (opcional)
                .param("titulo", "Producto Actualizado")
                .param("descripcion", "Desc Actualizada")
                .param("categoriaId", "1") 
                .param("precio", "150.0")
                .param("estado", "vendido")
                .param("propietarioId", "1")
                .with(request -> { request.setMethod("PUT"); return request; }) // Simular PUT
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());
    }

    @Test
    public void actualizarProducto_noExiste_retornaNotFound() throws Exception {
   //     when(productoService.actualizarProducto(eq(99L), any(Producto.class), eq(1L), anyList()))
    //        .thenThrow(new NoSuchElementException("No encontrado"));

        mockMvc.perform(multipart("/api/productos/actualizar/99")
                .param("titulo", "Test")
                .param("categoriaId", "1")
                .param("descripcion", "d")
                .param("precio", "1")
                .param("estado", "e")
                .param("propietarioId", "1")
                .with(request -> { request.setMethod("PUT"); return request; })
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isNotFound());
    }

    @Test
    public void actualizarEstadoProducto_valido_retornaOk() throws Exception {
        String nuevoEstado = "vendido";
        productoMock.setEstado(nuevoEstado); // El estado que se espera de vuelta

        when(productoService.actualizarEstadoProducto(1L, nuevoEstado)).thenReturn(productoMock);
        
        mockMvc.perform(put("/api/productos/{id}/estado", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoEstado))) // El body es solo un String
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("vendido"));
    }

    // --- Tests de Endpoints DELETE ---

    @Test
    public void eliminarProducto_existe_retornaNoContent() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/productos/eliminar/1"))
                .andExpect(status().isNoContent()); // 204
    }

    @Test
    public void eliminarProducto_noExiste_retornaNotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(productoService).eliminarProducto(99L);

        mockMvc.perform(delete("/api/productos/eliminar/99"))
                .andExpect(status().isNotFound());
    }
}