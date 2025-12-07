package com.example.demo.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.lenient;


import java.math.BigDecimal;
import java.util.Arrays;

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

import com.example.demo.DTO.ItemPagoDTO;
import com.example.demo.DTO.PagoCarritoDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.PagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.resources.preference.Preference;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoController - Pruebas Unitarias")
class PagoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private PagoController pagoController;

    private ObjectMapper objectMapper;
    private PagoCarritoDTO carritoDTO;
    private Preference preference;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pagoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        // Setup ItemPagoDTO
        ItemPagoDTO item1 = new ItemPagoDTO();
        item1.setTitulo("Laptop HP");
        item1.setPrecio(new BigDecimal("1500000"));

        ItemPagoDTO item2 = new ItemPagoDTO();
        item2.setTitulo("Mouse Logitech");
        item2.setPrecio(new BigDecimal("50000"));

        // Setup PagoCarritoDTO
        carritoDTO = new PagoCarritoDTO();
        carritoDTO.setItems(Arrays.asList(item1, item2));

        // Setup Preference con stubs lenient (permisivos)
        preference = mock(Preference.class);
        lenient().when(preference.getId()).thenReturn("MP-PREF-123456789");
        lenient().when(preference.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789");
        lenient().when(preference.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789");
    }

    // Método auxiliar para configurar preference con stubs
    private void setupPreferenceStubs(Preference pref, String id, String initPoint, String sandboxInitPoint) {
        when(pref.getId()).thenReturn(id);
        when(pref.getInitPoint()).thenReturn(initPoint);
        when(pref.getSandboxInitPoint()).thenReturn(sandboxInitPoint);
    }

    // ==================== Tests para crearPreferencia ====================

    @Test
    @DisplayName("POST /api/pago/crear - Debería crear preferencia exitosamente")
    void testCrearPreferencia_Exitoso() throws Exception {
        // Arrange
        setupPreferenceStubs(preference,
                "MP-PREF-123456789",
                "https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789",
                "https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789");

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MP-PREF-123456789"))
                .andExpect(jsonPath("$.initPoint").value("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789"))
                .andExpect(jsonPath("$.sandboxInitPoint").value("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456789"));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería crear preferencia con un solo item")
    void testCrearPreferencia_UnSoloItem() throws Exception {
        // Arrange
        ItemPagoDTO item = new ItemPagoDTO();
        item.setTitulo("Producto Único");
        item.setPrecio(new BigDecimal("100000"));

        PagoCarritoDTO carritoUnico = new PagoCarritoDTO();
        carritoUnico.setItems(Arrays.asList(item));

        Preference preferenciaUnica = mock(Preference.class);
        when(preferenciaUnica.getId()).thenReturn("MP-PREF-SINGLE");
        when(preferenciaUnica.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-SINGLE");

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preferenciaUnica);

        String requestBody = objectMapper.writeValueAsString(carritoUnico);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MP-PREF-SINGLE"))
                .andExpect(jsonPath("$.initPoint").value("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-SINGLE"));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar error del servicio")
    void testCrearPreferencia_ErrorServicio() throws Exception {
        // Arrange
        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class)))
                .thenThrow(new RuntimeException("Error creando preferencia de pago"));

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al crear la preferencia")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error creando preferencia de pago")));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar error de API de MercadoPago")
    void testCrearPreferencia_ErrorMercadoPago() throws Exception {
        // Arrange
        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class)))
                .thenThrow(new RuntimeException("MercadoPago API error: Invalid credentials"));

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al crear la preferencia")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid credentials")));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar carrito con múltiples items")
    void testCrearPreferencia_MultiplesItems() throws Exception {
        // Arrange
        ItemPagoDTO item1 = new ItemPagoDTO();
        item1.setTitulo("Producto 1");
        item1.setPrecio(new BigDecimal("10000"));

        ItemPagoDTO item2 = new ItemPagoDTO();
        item2.setTitulo("Producto 2");
        item2.setPrecio(new BigDecimal("20000"));

        ItemPagoDTO item3 = new ItemPagoDTO();
        item3.setTitulo("Producto 3");
        item3.setPrecio(new BigDecimal("30000"));

        PagoCarritoDTO carritoMultiple = new PagoCarritoDTO();
        carritoMultiple.setItems(Arrays.asList(item1, item2, item3));

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoMultiple);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar precios con decimales")
    void testCrearPreferencia_PreciosDecimales() throws Exception {
        // Arrange
        ItemPagoDTO item = new ItemPagoDTO();
        item.setTitulo("Producto con decimales");
        item.setPrecio(new BigDecimal("99.99"));

        PagoCarritoDTO carritoDecimal = new PagoCarritoDTO();
        carritoDecimal.setItems(Arrays.asList(item));

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoDecimal);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MP-PREF-123456789"));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería rechazar request sin Content-Type")
    void testCrearPreferencia_SinContentType() throws Exception {
        // Arrange
        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .content(requestBody))
                .andExpect(status().is4xxClientError());

        verify(pagoService, never()).crearPreferencia(any());
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar JSON malformado")
    void testCrearPreferencia_JSONMalformado() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().is4xxClientError());

        verify(pagoService, never()).crearPreferencia(any());
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar títulos con caracteres especiales")
    void testCrearPreferencia_CaracteresEspeciales() throws Exception {
        // Arrange
        ItemPagoDTO item = new ItemPagoDTO();
        item.setTitulo("Producto con ñ, tildes áéíóú y símbolos @#$");
        item.setPrecio(new BigDecimal("50000"));

        PagoCarritoDTO carritoEspecial = new PagoCarritoDTO();
        carritoEspecial.setItems(Arrays.asList(item));

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoEspecial);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    @Test
    @DisplayName("POST /api/pago/crear - Debería manejar NullPointerException")
    void testCrearPreferencia_NullPointerException() throws Exception {
        // Arrange
        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class)))
                .thenThrow(new NullPointerException("Carrito DTO es null"));

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al crear la preferencia")));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }

    // ==================== Tests de integración de endpoints ====================

    @Test
    @DisplayName("Debería validar CORS headers")
    void testCorsConfiguration() throws Exception {
        // Arrange
        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .header("Origin", "http://localhost:4200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Debería validar que el endpoint usa /api/pago")
    void testBaseMapping() throws Exception {
        // Arrange
        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preference);

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Verifica que el endpoint base está configurado correctamente
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // Verifica que sin /api no funciona
        mockMvc.perform(post("/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Debería usar método POST y no GET")
    void testMetodoHTTP() throws Exception {
        // Act & Assert - GET no debería funcionar
        mockMvc.perform(get("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());

        verify(pagoService, never()).crearPreferencia(any());
    }

    @Test
    @DisplayName("Debería retornar la preferencia completa con todos los campos")
    void testCrearPreferencia_PreferenciaCompleta() throws Exception {
        // Arrange
        Preference preferenciaCompleta = mock(Preference.class);
        when(preferenciaCompleta.getId()).thenReturn("MP-PREF-COMPLETE");
        when(preferenciaCompleta.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-COMPLETE");
        when(preferenciaCompleta.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-COMPLETE");

        when(pagoService.crearPreferencia(any(PagoCarritoDTO.class))).thenReturn(preferenciaCompleta);

        String requestBody = objectMapper.writeValueAsString(carritoDTO);

        // Act & Assert
        mockMvc.perform(post("/api/pago/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MP-PREF-COMPLETE"))
                .andExpect(jsonPath("$.initPoint").value("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-COMPLETE"))
                .andExpect(jsonPath("$.sandboxInitPoint").value("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-COMPLETE"));

        verify(pagoService).crearPreferencia(any(PagoCarritoDTO.class));
    }
}