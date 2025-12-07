package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.CarritoItem;
import com.example.demo.entity.HistorialCompra;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.HistorialCompraRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistorialCompraService - Pruebas Unitarias")
class HistorialCompraServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private HistorialCompraRepository historialCompraRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private HistorialCompraService historialCompraService;

    private User usuario;
    private Carrito carrito;
    private Producto producto1;
    private Producto producto2;
    private CarritoItem item1;
    private CarritoItem item2;
    private HistorialCompra historialCompra;

    @BeforeEach
    void setUp() {
        // Setup User
        usuario = new User();
        usuario.setId(1L);
        usuario.setCedula("123456789");
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Juan");

        // Setup Productos
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setTitulo("Laptop HP");
        producto1.setPrecio(1500000.0);
        producto1.setEstado("DISPONIBLE");

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setTitulo("Mouse Logitech");
        producto2.setPrecio(50000.0);
        producto2.setEstado("DISPONIBLE");

        // Setup CarritoItems
        item1 = new CarritoItem();
        item1.setId(1L);
        item1.setProducto(producto1);

        item2 = new CarritoItem();
        item2.setId(2L);
        item2.setProducto(producto2);

        // Setup Carrito
        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUser(usuario);
        carrito.setItems(new ArrayList<>(Arrays.asList(item1, item2)));

        // Setup HistorialCompra
        historialCompra = new HistorialCompra();
        historialCompra.setId(1L);
        historialCompra.setUsuario(usuario);
        historialCompra.setFechaCompra(LocalDateTime.now());
        historialCompra.setProductos(new ArrayList<>());
    }

    // ==================== Tests para generarHistorial ====================

    @Test
    @DisplayName("Debería generar historial exitosamente con múltiples productos")
    void testGenerarHistorial_Exitoso() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        // Act
        HistorialCompra resultado = historialCompraService.generarHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUsuario());
        assertNotNull(resultado.getFechaCompra());

        // Verificar que se guardó el historial 2 veces (antes y después de procesar items)
        verify(historialCompraRepository, times(2)).save(any(HistorialCompra.class));

        // Verificar que se actualizaron los productos
        verify(productoRepository, times(2)).save(any(Producto.class));

        // Verificar que los productos fueron marcados como VENDIDO
        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository, times(2)).save(productoCaptor.capture());

        List<Producto> productosGuardados = productoCaptor.getAllValues();
        productosGuardados.forEach(p -> assertEquals("VENDIDO", p.getEstado()));

        // Verificar que se eliminaron los items del carrito
        verify(carritoItemRepository).deleteByProducto_Id(1L);
        verify(carritoItemRepository).deleteByProducto_Id(2L);

        // Verificar que se limpió el carrito
        verify(carritoRepository).save(carrito);
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debería generar historial con un solo producto")
    void testGenerarHistorial_UnSoloProducto() {
        // Arrange
        carrito.setItems(new ArrayList<>(Arrays.asList(item1)));

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        // Act
        HistorialCompra resultado = historialCompraService.generarHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(producto1);
        verify(carritoItemRepository, times(1)).deleteByProducto_Id(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no existe")
    void testGenerarHistorial_UsuarioNoExiste() {
        // Arrange
        when(userRepository.findByCedula("999999999")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> historialCompraService.generarHistorial("999999999"));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userRepository).findByCedula("999999999");
        verify(carritoRepository, never()).findByUser_Cedula(anyString());
        verify(historialCompraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no tiene carrito")
    void testGenerarHistorial_UsuarioSinCarrito() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> historialCompraService.generarHistorial("123456789"));

        assertEquals("El usuario no tiene carrito", exception.getMessage());
        verify(userRepository).findByCedula("123456789");
        verify(carritoRepository).findByUser_Cedula("123456789");
        verify(historialCompraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si carrito está vacío")
    void testGenerarHistorial_CarritoVacio() {
        // Arrange
        carrito.setItems(new ArrayList<>());

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> historialCompraService.generarHistorial("123456789"));

        assertEquals("El carrito está vacío", exception.getMessage());
        verify(historialCompraRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería establecer fecha de compra actual")
    void testGenerarHistorial_FechaCompraActual() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenAnswer(invocation -> {
            HistorialCompra h = invocation.getArgument(0);
            h.setId(1L);
            return h;
        });
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        // Act
        HistorialCompra resultado = historialCompraService.generarHistorial("123456789");

        // Assert
        LocalDateTime despues = LocalDateTime.now().plusSeconds(1);

        assertNotNull(resultado.getFechaCompra());
        assertTrue(resultado.getFechaCompra().isAfter(antes));
        assertTrue(resultado.getFechaCompra().isBefore(despues));
    }

    @Test
    @DisplayName("Debería asociar productos al historial")
    void testGenerarHistorial_AsociarProductos() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        // Act
        historialCompraService.generarHistorial("123456789");

        // Assert
        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository, times(2)).save(productoCaptor.capture());

        List<Producto> productos = productoCaptor.getAllValues();
        productos.forEach(p -> {
            assertEquals(historialCompra, p.getHistorial());
            assertEquals("VENDIDO", p.getEstado());
        });
    }

    @Test
    @DisplayName("Debería limpiar carrito después de generar historial")
    void testGenerarHistorial_LimpiarCarrito() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        assertEquals(2, carrito.getItems().size());

        // Act
        historialCompraService.generarHistorial("123456789");

        // Assert
        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("Debería eliminar items del carrito después de la compra")
    void testGenerarHistorial_EliminarItems() {
        // Arrange
        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        // Act
        historialCompraService.generarHistorial("123456789");

        // Assert
        verify(carritoItemRepository).deleteByProducto_Id(1L);
        verify(carritoItemRepository).deleteByProducto_Id(2L);
        verify(carritoItemRepository, times(2)).deleteByProducto_Id(anyLong());
    }

    @Test
    @DisplayName("Debería manejar carrito con muchos productos")
    void testGenerarHistorial_MuchosProductos() {
        // Arrange
        List<CarritoItem> muchoItems = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Producto p = new Producto();
            p.setId((long) i);
            p.setTitulo("Producto " + i);
            p.setEstado("DISPONIBLE");

            CarritoItem item = new CarritoItem();
            item.setId((long) i);
            item.setProducto(p);

            muchoItems.add(item);
        }
        carrito.setItems(muchoItems);

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789")).thenReturn(Optional.of(carrito));
        when(historialCompraRepository.save(any(HistorialCompra.class))).thenReturn(historialCompra);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        doNothing().when(carritoItemRepository).deleteByProducto_Id(anyLong());

        // Act
        HistorialCompra resultado = historialCompraService.generarHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(10)).save(any(Producto.class));
        verify(carritoItemRepository, times(10)).deleteByProducto_Id(anyLong());
    }

    // ==================== Tests para obtenerHistorial ====================

    @Test
    @DisplayName("Debería obtener historial de compras del usuario")
    void testObtenerHistorial_Exitoso() {
        // Arrange
        HistorialCompra h1 = new HistorialCompra();
        h1.setId(1L);
        h1.setFechaCompra(LocalDateTime.now());

        HistorialCompra h2 = new HistorialCompra();
        h2.setId(2L);
        h2.setFechaCompra(LocalDateTime.now().minusDays(1));

        List<HistorialCompra> historial = Arrays.asList(h1, h2);

        when(historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc("123456789"))
                .thenReturn(historial);

        // Act
        List<HistorialCompra> resultado = historialCompraService.obtenerHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(h1, resultado.get(0));
        assertEquals(h2, resultado.get(1));
        verify(historialCompraRepository).findByUsuario_CedulaOrderByFechaCompraDesc("123456789");
    }

    @Test
    @DisplayName("Debería retornar lista vacía si usuario no tiene historial")
    void testObtenerHistorial_SinHistorial() {
        // Arrange
        when(historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc("123456789"))
                .thenReturn(new ArrayList<>());

        // Act
        List<HistorialCompra> resultado = historialCompraService.obtenerHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(historialCompraRepository).findByUsuario_CedulaOrderByFechaCompraDesc("123456789");
    }

    @Test
    @DisplayName("Debería ordenar historial por fecha descendente")
    void testObtenerHistorial_OrdenadoPorFecha() {
        // Arrange
        HistorialCompra h1 = new HistorialCompra();
        h1.setId(1L);
        h1.setFechaCompra(LocalDateTime.now());

        HistorialCompra h2 = new HistorialCompra();
        h2.setId(2L);
        h2.setFechaCompra(LocalDateTime.now().minusDays(5));

        HistorialCompra h3 = new HistorialCompra();
        h3.setId(3L);
        h3.setFechaCompra(LocalDateTime.now().minusDays(2));

        // Ya ordenado por el repositorio (DESC)
        List<HistorialCompra> historial = Arrays.asList(h1, h3, h2);

        when(historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc("123456789"))
                .thenReturn(historial);

        // Act
        List<HistorialCompra> resultado = historialCompraService.obtenerHistorial("123456789");

        // Assert
        assertEquals(3, resultado.size());
        assertEquals(h1, resultado.get(0)); // Más reciente
        assertEquals(h3, resultado.get(1)); // Medio
        assertEquals(h2, resultado.get(2)); // Más antiguo
    }

    @Test
    @DisplayName("Debería obtener historial con múltiples compras")
    void testObtenerHistorial_MultipleCompras() {
        // Arrange
        List<HistorialCompra> historial = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            HistorialCompra h = new HistorialCompra();
            h.setId((long) i);
            h.setFechaCompra(LocalDateTime.now().minusDays(i));
            historial.add(h);
        }

        when(historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc("123456789"))
                .thenReturn(historial);

        // Act
        List<HistorialCompra> resultado = historialCompraService.obtenerHistorial("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals(5, resultado.size());
        verify(historialCompraRepository).findByUsuario_CedulaOrderByFechaCompraDesc("123456789");
    }

    @Test
    @DisplayName("Debería manejar cédulas con diferentes formatos")
    void testObtenerHistorial_DiferentesFormatos() {
        // Arrange
        List<HistorialCompra> historial = Arrays.asList(historialCompra);

        when(historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc("ABC-123"))
                .thenReturn(historial);

        // Act
        List<HistorialCompra> resultado = historialCompraService.obtenerHistorial("ABC-123");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(historialCompraRepository).findByUsuario_CedulaOrderByFechaCompraDesc("ABC-123");
    }
}
