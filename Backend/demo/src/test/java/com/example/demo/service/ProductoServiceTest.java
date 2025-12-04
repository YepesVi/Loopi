package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryUploadService cloudinaryUploadService;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==========================================================
    //          TEST: obtenerPorId()
    // ==========================================================
    @Test
    void obtenerPorId_ok() {
        Producto p = new Producto();
        p.setId(1L);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(p));

        Producto result = productoService.obtenerPorId(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> productoService.obtenerPorId(99L));
    }

    // ==========================================================
    //          TEST: crearProducto()
    // ==========================================================
    @Test
    void crearProducto_ok() throws IOException {
        Producto producto = new Producto();

        Categoria categoria = new Categoria();
        categoria.setId(7L);

        User user = new User();
        user.setId(3L);

        MultipartFile fileMock = mock(MultipartFile.class);

        when(fileMock.isEmpty()).thenReturn(false);

        when(categoriaService.findById(7L))
                .thenReturn(Optional.of(categoria));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(user));

        when(cloudinaryUploadService.uploadFile(any(), eq("productos")))
                .thenReturn(Map.of("secure_url", "url.jpg", "public_id", "img123"));

        when(productoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Producto result = productoService.crearProducto(
                producto, 7L, 3L, List.of(fileMock)
        );

        assertNotNull(result.getCategoria());
        assertNotNull(result.getPropietario());
        assertEquals(1, result.getImagenes().size());
    }

    // ==========================================================
    //          TEST: actualizarProducto()
    // ==========================================================
    @Test
    void actualizarProducto_ok() throws IOException {

        Producto existente = new Producto();
        existente.setId(1L);

        Imagen oldImg = new Imagen();
        oldImg.setPublicId("old123");
        existente.getImagenes().add(oldImg);

        Producto detalles = new Producto();
        detalles.setTitulo("Nuevo");
        detalles.setDescripcion("Desc");
        detalles.setEstado("publicado");
        detalles.setPrecio(99.0);

        Categoria nuevaCat = new Categoria();
        nuevaCat.setId(10L);

        User nuevoProp = new User();
        nuevoProp.setId(8L);

        MultipartFile nuevoArchivo = mock(MultipartFile.class);
        when(nuevoArchivo.isEmpty()).thenReturn(false);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaService.findById(10L)).thenReturn(Optional.of(nuevaCat));
        when(userRepository.findById(8L)).thenReturn(Optional.of(nuevoProp));

        when(cloudinaryUploadService.uploadFile(any(), eq("productos")))
                .thenReturn(Map.of("secure_url", "new.jpg", "public_id", "new1"));

        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Producto resultado = productoService.actualizarProducto(
                1L, detalles, 10L, 8L, List.of(nuevoArchivo)
        );

        assertEquals("Nuevo", resultado.getTitulo());
        assertEquals(1, resultado.getImagenes().size());
        verify(cloudinaryUploadService).deleteFile("old123");
    }

    // ==========================================================
    //          TEST: eliminarProducto()
    // ==========================================================
    @Test
    void eliminarProducto_ok() throws IOException {

        Imagen img = new Imagen();
        img.setPublicId("abc123");

        Producto producto = new Producto();
        producto.setId(5L);
        producto.getImagenes().add(img);

        when(productoRepository.existsById(5L)).thenReturn(true);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        productoService.eliminarProducto(5L);

        verify(cloudinaryUploadService).deleteFile("abc123");
        verify(productoRepository).deleteById(5L);
    }

    // ==========================================================
    //          TEST: historialPublicaciones()
    // ==========================================================
    @Test
    void historialPublicaciones_filtrado() {

        Producto p1 = new Producto();
        p1.setEstado("publicado");

        Producto p2 = new Producto();
        p2.setEstado("vendido");

        when(productoRepository.findByPropietarioId(10L))
                .thenReturn(List.of(p1, p2));

        List<Producto> resultado = productoService.historialPublicaciones(10L, "publicado");

        assertEquals(1, resultado.size());
    }

    // ==========================================================
    //          TEST: obtenerPublicados()
    // ==========================================================
    @Test
    void obtenerPublicados_ok() {

        Producto p = new Producto();
        p.setEstado("publicado");

        when(productoRepository.findByEstadoIgnoreCase("publicado"))
                .thenReturn(List.of(p));

        List<Producto> resultado = productoService.obtenerPublicados();

        assertEquals(1, resultado.size());
    }

    // ==========================================================
    //          TEST: actualizarEstadoProducto()
    // ==========================================================
    @Test
    void actualizarEstadoProducto_ok() {

        Producto p = new Producto();
        p.setId(1L);
        p.setEstado("publicado");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Producto resultado = productoService.actualizarEstadoProducto(1L, "pausado");

        assertEquals("pausado", resultado.getEstado());
    }

    // ==========================================================
    //          TEST: buscarConFiltros()
    // ==========================================================
    @Test
    void buscarConFiltros_ok() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Producto> pageMock = new PageImpl<>(List.of(new Producto()));

        when(categoriaService.findAllDescendantIdsWithSelf(5L))
                .thenReturn(List.of(5L, 6L));

        when(productoRepository.buscarConFiltros(
                any(), any(), eq(2), any(), any(), any(), any(), eq(pageable)
        )).thenReturn(pageMock);

        Page<Producto> resultado = productoService.buscarConFiltros(
                "tv", 5L, 100.0, 500.0, "publicado", 10L, pageable
        );

        assertEquals(1, resultado.getTotalElements());
    }
}

