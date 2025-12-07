package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.entity.HistorialCompra;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.HistorialCompraService;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistorialCompraController - Pruebas Unitarias")
class HistorialCompraControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HistorialCompraService historialCompraService;

    @InjectMocks
    private HistorialCompraController historialCompraController;

    private User usuario;
    private HistorialCompra historial1;
    private HistorialCompra historial2;
    private List<HistorialCompra> listaHistorial;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(historialCompraController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Setup User
        usuario = new User();
        usuario.setId(1L);
        usuario.setCedula("123456789");
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Juan");

        // Setup Producto
        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setTitulo("Laptop HP");
        producto1.setPrecio(1500000.0);
        producto1.setEstado("VENDIDO");

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setTitulo("Mouse Logitech");
        producto2.setPrecio(50000.0);
        producto2.setEstado("VENDIDO");

        // Setup HistorialCompra 1
        historial1 = new HistorialCompra();
        historial1.setId(1L);
        historial1.setUsuario(usuario);
        historial1.setFechaCompra(LocalDateTime.now());
        historial1.setProductos(new ArrayList<>(Arrays.asList(producto1)));

        // Setup HistorialCompra 2
        historial2 = new HistorialCompra();
        historial2.setId(2L);
        historial2.setUsuario(usuario);
        historial2.setFechaCompra(LocalDateTime.now().minusDays(5));
        historial2.setProductos(new ArrayList<>(Arrays.asList(producto2)));

        listaHistorial = Arrays.asList(historial1, historial2);
    }

    // ==================== Tests para obtenerHistorial ====================

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería obtener historial exitosamente")
    void testObtenerHistorial_Exitoso() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería retornar 404 si no hay historial")
    void testObtenerHistorial_SinHistorial() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("999999999")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se encontró historial para este usuario."));

        verify(historialCompraService).obtenerHistorial("999999999");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería obtener historial con una sola compra")
    void testObtenerHistorial_UnaSolaCompra() throws Exception {
        // Arrange
        List<HistorialCompra> historialUnico = Arrays.asList(historial1);
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(historialUnico);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería manejar cédulas con caracteres especiales")
    void testObtenerHistorial_CedulaConCaracteresEspeciales() throws Exception {
        // Arrange
        String cedulaEspecial = "ABC-123-XYZ";
        when(historialCompraService.obtenerHistorial(cedulaEspecial)).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/" + cedulaEspecial))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(historialCompraService).obtenerHistorial(cedulaEspecial);
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería obtener historial con múltiples compras")
    void testObtenerHistorial_MultipleCompras() throws Exception {
        // Arrange
        List<HistorialCompra> muchasCompras = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            HistorialCompra h = new HistorialCompra();
            h.setId((long) i);
            h.setUsuario(usuario);
            h.setFechaCompra(LocalDateTime.now().minusDays(i));
            muchasCompras.add(h);
        }

        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(muchasCompras);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[9].id").value(10));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería verificar estructura del JSON")
    void testObtenerHistorial_EstructuraJSON() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].usuario").exists())
                .andExpect(jsonPath("$[0].fechaCompra").exists())
                .andExpect(jsonPath("$[0].productos").exists())
                .andExpect(jsonPath("$[0].usuario.cedula").value("123456789"))
                .andExpect(jsonPath("$[0].usuario.nombre").value("Juan"));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería verificar productos en el historial")
    void testObtenerHistorial_VerificarProductos() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productos").isArray())
                .andExpect(jsonPath("$[0].productos[0].titulo").value("Laptop HP"))
                .andExpect(jsonPath("$[0].productos[0].precio").value(1500000.0))
                .andExpect(jsonPath("$[0].productos[0].estado").value("VENDIDO"));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería manejar cédulas numéricas largas")
    void testObtenerHistorial_CedulaNumerica() throws Exception {
        // Arrange
        String cedulaLarga = "1234567890123";
        when(historialCompraService.obtenerHistorial(cedulaLarga)).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/" + cedulaLarga))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(historialCompraService).obtenerHistorial(cedulaLarga);
    }

    @Test
    @DisplayName("GET /api/historial-compra/{cedula} - Debería manejar excepción del servicio")
    void testObtenerHistorial_ExcepcionServicio() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789"))
                .thenThrow(new RuntimeException("Error al consultar base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al consultar base de datos")));

        verify(historialCompraService).obtenerHistorial("123456789");
    }



    // ==================== Tests de integración de endpoints ====================

    @Test
    @DisplayName("Debería validar CORS headers")
    void testCorsConfiguration() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Debería validar que el endpoint usa /api/historial-compra")
    void testBaseMapping() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Verifica que el endpoint base está configurado correctamente
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk());

        // Verifica que sin /api no funciona
        mockMvc.perform(get("/historial-compra/123456789"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería usar método GET y no POST")
    void testMetodoHTTP() throws Exception {
        // Act & Assert - POST no debería funcionar
        mockMvc.perform(post("/api/historial-compra/123456789")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(historialCompraService, never()).obtenerHistorial(anyString());
    }

    @Test
    @DisplayName("Debería manejar cédulas con espacios")
    void testObtenerHistorial_CedulaConEspacios() throws Exception {
        // Arrange
        // Spring automáticamente hace trim en path variables
        when(historialCompraService.obtenerHistorial("123 456 789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123 456 789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(historialCompraService).obtenerHistorial("123 456 789");
    }

    @Test
    @DisplayName("Debería retornar Content-Type application/json para respuestas exitosas")
    void testContentType_Exitoso() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(listaHistorial);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(historialCompraService).obtenerHistorial("123456789");
    }

    @Test
    @DisplayName("Debería retornar el mensaje correcto cuando no hay historial")
    void testObtenerHistorial_MensajeError404() throws Exception {
        // Arrange
        when(historialCompraService.obtenerHistorial("123456789")).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/123456789"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("No se encontró historial para este usuario."));

        verify(historialCompraService).obtenerHistorial("123456789");
    }


    @Test
    @DisplayName("Debería procesar múltiples solicitudes para diferentes usuarios")
    void testObtenerHistorial_MultiplesUsuarios() throws Exception {
        // Arrange
        List<HistorialCompra> historialUsuario1 = Arrays.asList(historial1);
        List<HistorialCompra> historialUsuario2 = Arrays.asList(historial2);

        when(historialCompraService.obtenerHistorial("111111111")).thenReturn(historialUsuario1);
        when(historialCompraService.obtenerHistorial("222222222")).thenReturn(historialUsuario2);

        // Act & Assert
        mockMvc.perform(get("/api/historial-compra/111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/historial-compra/222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(historialCompraService).obtenerHistorial("111111111");
        verify(historialCompraService).obtenerHistorial("222222222");
    }
}
