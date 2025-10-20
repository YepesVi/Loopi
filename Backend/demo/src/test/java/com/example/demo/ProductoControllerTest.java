package com.example.demo;

import com.example.demo.controller.ProductoController;
import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductoController.class)
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void crearProducto_valido_retornaOkYProductoCreado() throws Exception {
        Producto producto = new Producto();
        producto.setTitulo("Producto Test");
        producto.setDescripcion("Descripción de prueba");
        producto.setCategoria("General");
        producto.setPrecio(10000.0);
        producto.setEstado("PUBLICADO");
        producto.setFotos("foto.jpg");
        producto.setPropietarioId(1L);

        when(repo.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Producto Test"));
    }

    @Test
    public void crearProducto_invalido_retornaBadRequestYErrores() throws Exception {
        Producto producto = new Producto();

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void listarProductosPublicos_paginado_retornaPaginaCorrecta() throws Exception {
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setEstado("PUBLICADO");
        p1.setTitulo("Producto 1");

        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setEstado("PUBLICADO");
        p2.setTitulo("Producto 2");

        List<Producto> productos = List.of(p1, p2);
        Page<Producto> pagina = new PageImpl<>(productos);

        when(repo.findByEstadoIgnoreCase(eq("PUBLICADO"), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/productos/publicados")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "titulo,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(productos.size()))
                .andExpect(jsonPath("$.content[0].estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.content[1].estado").value("PUBLICADO"));
    }

    @Test
    public void listarPublicacionesPorPropietario_retornaPaginaCorrecta() throws Exception {
        Long propietarioId = 10L;

        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setPropietarioId(propietarioId);
        p1.setTitulo("Producto A");

        Producto p2 = new Producto();
        p2.setId(2L);
        p2.setPropietarioId(propietarioId);
        p2.setTitulo("Producto B");

        List<Producto> productos = List.of(p1, p2);
        Page<Producto> pagina = new PageImpl<>(productos);

        when(repo.findByPropietarioId(eq(propietarioId), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/productos/mis-publicaciones")
                .param("propietarioId", propietarioId.toString())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(productos.size()))
                .andExpect(jsonPath("$.content[0].titulo").value("Producto A"))
                .andExpect(jsonPath("$.content[1].titulo").value("Producto B"));
    }

    @Test
    public void actualizarProducto_propietarioCorrecto_retornaOk() throws Exception {
        Long id = 1L;
        Long usuarioId = 10L;

        Producto productoOriginal = new Producto();
        productoOriginal.setId(id);
        productoOriginal.setPropietarioId(usuarioId);

        Producto productoActualizado = new Producto();
        productoActualizado.setTitulo("Producto Actualizado");
        productoActualizado.setDescripcion("Descripción");
        productoActualizado.setCategoria("Cat");
        productoActualizado.setPrecio(100.0);
        productoActualizado.setEstado("PUBLICADO");
        productoActualizado.setPropietarioId(usuarioId);

        when(repo.findById(id)).thenReturn(Optional.of(productoOriginal));
        when(repo.save(any(Producto.class))).thenReturn(productoActualizado);

        mockMvc.perform(put("/api/productos/{id}", id)
                .param("usuarioId", usuarioId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk());
    }

    @Test
    public void actualizarProducto_propietarioIncorrecto_retornaForbidden() throws Exception {
        Long id = 1L;
        Long usuarioId = 10L;
        Long otroUsuarioId = 20L;

        Producto productoOriginal = new Producto();
        productoOriginal.setId(id);
        productoOriginal.setPropietarioId(otroUsuarioId);

        Producto productoActualizado = new Producto();
        productoActualizado.setTitulo("Producto Actualizado");
        productoActualizado.setDescripcion("Descripción");
        productoActualizado.setCategoria("Cat");
        productoActualizado.setPrecio(100.0);
        productoActualizado.setEstado("PUBLICADO");
        productoActualizado.setPropietarioId(usuarioId);

        when(repo.findById(id)).thenReturn(Optional.of(productoOriginal));

        mockMvc.perform(put("/api/productos/{id}", id)
                .param("usuarioId", usuarioId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void eliminarProducto_propietarioCorrecto_retornaOk() throws Exception {
        Long id = 1L;
        Long usuarioId = 10L;

        Producto productoOriginal = new Producto();
        productoOriginal.setId(id);
        productoOriginal.setPropietarioId(usuarioId);

        when(repo.findById(id)).thenReturn(Optional.of(productoOriginal));

        mockMvc.perform(delete("/api/productos/{id}", id)
                .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void eliminarProducto_propietarioIncorrecto_retornaForbidden() throws Exception {
        Long id = 1L;
        Long usuarioId = 10L;
        Long otroUsuarioId = 20L;

        Producto productoOriginal = new Producto();
        productoOriginal.setId(id);
        productoOriginal.setPropietarioId(otroUsuarioId);

        when(repo.findById(id)).thenReturn(Optional.of(productoOriginal));

        mockMvc.perform(delete("/api/productos/{id}", id)
                .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
public void buscarProductos_conFiltros_retornaResultados() throws Exception {
    Producto p1 = new Producto();
    p1.setTitulo("Producto A");
    p1.setCategoria("Electrónica");
    p1.setPrecio(1500.0);

    Producto p2 = new Producto();
    p2.setTitulo("Producto B");
    p2.setCategoria("Electrónica");
    p2.setPrecio(2000.0);

    List<Producto> productos = List.of(p1, p2);
    Page<Producto> pagina = new PageImpl<>(productos);

    when(repo.buscarConFiltros(anyString(), anyString(), anyDouble(), anyDouble(), any(Pageable.class)))
        .thenReturn(pagina);

    mockMvc.perform(get("/api/productos/buscar")
            .param("titulo", "Producto")
            .param("categoria", "Electrónica")
            .param("precioMin", "1000")
            .param("precioMax", "2500")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(productos.size()))
        .andExpect(jsonPath("$.content[0].titulo").value("Producto A"))
        .andExpect(jsonPath("$.content[1].titulo").value("Producto B"));
}

}
