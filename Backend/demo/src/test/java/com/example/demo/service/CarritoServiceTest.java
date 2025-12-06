package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService - Pruebas Unitarias")
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private HistorialCompraService historialCompraService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CarritoService carritoService;

    private User usuario;
    private Carrito carrito;
    private Producto producto;
    private CarritoItem carritoItem;

    @BeforeEach
    void setUp() {
        // Configuración de objetos de prueba
        usuario = new User();
        usuario.setCedula("123456789");
        usuario.setCorreo("test@example.com");

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUser(usuario);
        carrito.setItems(new ArrayList<>());

        producto = new Producto();
        producto.setId(1L);
        producto.setTitulo("Producto Test");
        producto.setEstado("Disponible");
        producto.setPrecio(100.0);

        carritoItem = new CarritoItem();
        carritoItem.setId(1L);
        carritoItem.setCarrito(carrito);
        carritoItem.setProducto(producto);
    }

    // ==================== Tests para getCarritoDeUsuario ====================

    @Test
    @DisplayName("Debería obtener carrito existente del usuario")
    void testGetCarritoDeUsuario_CarritoExistente() {
        // Arrange
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));

        // Act
        Carrito resultado = carritoService.getCarritoDeUsuario("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals(carrito.getId(), resultado.getId());
        verify(carritoRepository).findByUser_Cedula("123456789");
        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería crear nuevo carrito si usuario no tiene uno")
    void testGetCarritoDeUsuario_CarritoNoExistente() {
        // Arrange
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.empty());
        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // Act
        Carrito resultado = carritoService.getCarritoDeUsuario("123456789");

        // Assert
        assertNotNull(resultado);
        verify(carritoRepository,times(2)).findByUser_Cedula("123456789");
        verify(userRepository).findByCedula("123456789");
        verify(carritoRepository).save(any(Carrito.class));
    }

    // ==================== Tests para crearCarritoParaUsuario ====================

    @Test
    @DisplayName("Debería crear carrito exitosamente para usuario sin carrito")
    void testCrearCarritoParaUsuario_Exitoso() {
        // Arrange
        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // Act
        Carrito resultado = carritoService.crearCarritoParaUsuario("123456789");

        // Assert
        assertNotNull(resultado);
        assertEquals(usuario, resultado.getUser());
        verify(userRepository).findByCedula("123456789");
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no existe")
    void testCrearCarritoParaUsuario_UsuarioNoExiste() {
        // Arrange
        when(userRepository.findByCedula("999999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.crearCarritoParaUsuario("999999999")
        );

        assertEquals("Usuario no existe", exception.getMessage());
        verify(userRepository).findByCedula("999999999");
        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario ya tiene carrito activo")
    void testCrearCarritoParaUsuario_CarritoYaExiste() {
        // Arrange
        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.crearCarritoParaUsuario("123456789")
        );

        assertEquals("El usuario ya tiene un carrito activo.", exception.getMessage());
        verify(carritoRepository, never()).save(any());
    }

    // ==================== Tests para agregarProducto ====================

    @Test
    @DisplayName("Debería agregar producto exitosamente al carrito")
    void testAgregarProducto_Exitoso() {
        // Arrange
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);
        when(carritoItemRepository.save(any(CarritoItem.class)))
                .thenReturn(carritoItem);

        // Act
        Carrito resultado = carritoService.agregarProducto("123456789", 1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository).findById(1L);
        verify(carritoItemRepository).save(any(CarritoItem.class));
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si producto no existe")
    void testAgregarProducto_ProductoNoExiste() {
        // Arrange
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(productoRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto("123456789", 999L)
        );

        assertEquals("Producto no existe", exception.getMessage());
        verify(carritoItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si producto está vendido")
    void testAgregarProducto_ProductoVendido() {
        // Arrange
        producto.setEstado("Vendido");
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto("123456789", 1L)
        );

        assertEquals("No hay disponibilidad de este producto.", exception.getMessage());
        verify(carritoItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si producto ya está en el carrito")
    void testAgregarProducto_ProductoYaEnCarrito() {
        // Arrange
        carrito.getItems().add(carritoItem);
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto("123456789", 1L)
        );

        assertEquals("Este producto ya está agregado al carrito.", exception.getMessage());
        verify(carritoItemRepository, never()).save(any());
    }

    // ==================== Tests para eliminarItem ====================

    @Test
    @DisplayName("Debería eliminar item del carrito correctamente")
    void testEliminarItem() {
        // Act
        carritoService.eliminarItem(1L);

        // Assert
        verify(carritoItemRepository).deleteById(1L);
    }

    // ==================== Tests para vaciarCarrito ====================

    @Test
    @DisplayName("Debería vaciar carrito correctamente")
    void testVaciarCarrito() {
        // Arrange
        List<CarritoItem> items = new ArrayList<>();
        items.add(carritoItem);
        carrito.setItems(items);

        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        doNothing().when(carritoItemRepository).deleteAll(anyList());
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // Act
        carritoService.vaciarCarrito("123456789");

        // Assert
        verify(carritoItemRepository).deleteAll(anyList());
        verify(carritoRepository).save(carrito);
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debería manejar vaciado de carrito ya vacío")
    void testVaciarCarrito_CarritoVacio() {
        // Arrange
        carrito.setItems(new ArrayList<>());
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // Act
        carritoService.vaciarCarrito("123456789");

        // Assert
        verify(carritoItemRepository).deleteAll(anyList());
        verify(carritoRepository).save(carrito);
    }

    // ==================== Tests para realizarCompra ====================

    @Test
    @DisplayName("Debería realizar compra exitosamente")
    void testRealizarCompra_Exitoso() {
        // Arrange
        carrito.getItems().add(carritoItem);
        HistorialCompra historial = new HistorialCompra();

        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));
        when(historialCompraService.generarHistorial("123456789"))
                .thenReturn(historial);
        doNothing().when(emailService).sendPurchaseEmail(anyString(), any());

        // Act
        HistorialCompra resultado = carritoService.realizarCompra("123456789");

        // Assert
        assertNotNull(resultado);
        verify(userRepository).findByCedula("123456789");
        verify(carritoRepository).findByUser_Cedula("123456789");
        verify(historialCompraService).generarHistorial("123456789");
        verify(emailService).sendPurchaseEmail(usuario.getCorreo(), historial);
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no existe al realizar compra")
    void testRealizarCompra_UsuarioNoExiste() {
        // Arrange
        when(userRepository.findByCedula("999999999"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> carritoService.realizarCompra("999999999")
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(historialCompraService, never()).generarHistorial(anyString());
        verify(emailService, never()).sendPurchaseEmail(anyString(), any());
    }

    @Test
    @DisplayName("Debería lanzar excepción si usuario no tiene carrito")
    void testRealizarCompra_CarritoNoExiste() {
        // Arrange
        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> carritoService.realizarCompra("123456789")
        );

        assertEquals("El usuario no tiene carrito", exception.getMessage());
        verify(historialCompraService, never()).generarHistorial(anyString());
    }

    @Test
    @DisplayName("Debería lanzar excepción si carrito está vacío al realizar compra")
    void testRealizarCompra_CarritoVacio() {
        // Arrange
        carrito.setItems(new ArrayList<>());
        when(userRepository.findByCedula("123456789"))
                .thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUser_Cedula("123456789"))
                .thenReturn(Optional.of(carrito));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> carritoService.realizarCompra("123456789")
        );

        assertEquals("El carrito está vacío", exception.getMessage());
        verify(historialCompraService, never()).generarHistorial(anyString());
        verify(emailService, never()).sendPurchaseEmail(anyString(), any());
    }
}
