package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.DTO.ItemPagoDTO;
import com.example.demo.DTO.PagoCarritoDTO;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.preference.Preference;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService - Pruebas Unitarias")
class PagoServiceTest {

    private PagoService pagoService;

    @Mock
    private PreferenceClient preferenceClient;

    private PagoCarritoDTO carritoDTO;
    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = "TEST_ACCESS_TOKEN_123";
        pagoService = new PagoService(accessToken);

        // Setup del DTO de prueba
        ItemPagoDTO item1 = new ItemPagoDTO();
        item1.setTitulo("Laptop HP");
        item1.setPrecio(new BigDecimal("1500000"));

        ItemPagoDTO item2 = new ItemPagoDTO();
        item2.setTitulo("Mouse Logitech");
        item2.setPrecio(new BigDecimal("50000"));

        carritoDTO = new PagoCarritoDTO();
        carritoDTO.setItems(Arrays.asList(item1, item2));
    }

    // ==================== Tests para crearPreferencia ====================

    @Test
    @DisplayName("Debería crear preferencia exitosamente con múltiples items")
    void testCrearPreferencia_Exitoso() throws MPException, MPApiException {
        // Arrange
        // Mock de la preferencia esperada usando Mockito
        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-123456");
        when(preferenciaEsperada.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456");
        when(preferenciaEsperada.getSandboxInitPoint()).thenReturn("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenciaEsperada))) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoDTO);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-123456", resultado.getId());
            assertEquals("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456", resultado.getInitPoint());
            assertEquals("https://sandbox.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-123456", resultado.getSandboxInitPoint());

            mockedConfig.verify(() -> MercadoPagoConfig.setAccessToken(accessToken));
        }
    }


    @Test
    @DisplayName("Debería crear preferencia con un solo item")
    void testCrearPreferencia_UnSoloItem() throws MPException, MPApiException {
        // Arrange
        ItemPagoDTO item = new ItemPagoDTO();
        item.setTitulo("Producto Único");
        item.setPrecio(new BigDecimal("100000"));

        PagoCarritoDTO carritoUnico = new PagoCarritoDTO();
        carritoUnico.setItems(List.of(item));

        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-SINGLE");
        when(preferenciaEsperada.getInitPoint()).thenReturn("https://www.mercadopago.com/checkout/v1/redirect?pref_id=MP-PREF-SINGLE");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenciaEsperada))) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoUnico);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-SINGLE", resultado.getId());
            mockedConfig.verify(() -> MercadoPagoConfig.setAccessToken(accessToken));
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando MPApiException ocurre")
    void testCrearPreferencia_MPApiException() throws MPException, MPApiException {
        // Arrange
        MPResponse mockResponse = mock(MPResponse.class);
        when(mockResponse.getStatusCode()).thenReturn(400);
        when(mockResponse.getContent()).thenReturn("{\"error\":\"Invalid request\"}");

        MPApiException mpApiException = new MPApiException("API Error", mockResponse);

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenThrow(mpApiException))) {

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> pagoService.crearPreferencia(carritoDTO));

            assertEquals("Error creando preferencia de pago", exception.getMessage());
            assertTrue(exception.getCause() instanceof MPApiException);
            mockedConfig.verify(() -> MercadoPagoConfig.setAccessToken(accessToken));
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando MPException ocurre")
    void testCrearPreferencia_MPException() throws MPException, MPApiException {
        // Arrange
        MPException mpException = new MPException("Connection error");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenThrow(mpException))) {

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> pagoService.crearPreferencia(carritoDTO));

            assertEquals("Error creando preferencia de pago", exception.getMessage());
            assertTrue(exception.getCause() instanceof MPException);
            mockedConfig.verify(() -> MercadoPagoConfig.setAccessToken(accessToken));
        }
    }

    @Test
    @DisplayName("Debería manejar carrito con precios decimales")
    void testCrearPreferencia_PreciosDecimales() throws MPException, MPApiException {
        // Arrange
        ItemPagoDTO item1 = new ItemPagoDTO();
        item1.setTitulo("Producto con decimales");
        item1.setPrecio(new BigDecimal("99.99"));

        ItemPagoDTO item2 = new ItemPagoDTO();
        item2.setTitulo("Otro producto");
        item2.setPrecio(new BigDecimal("150.50"));

        PagoCarritoDTO carritoConDecimales = new PagoCarritoDTO();
        carritoConDecimales.setItems(Arrays.asList(item1, item2));

        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-DECIMAL");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenciaEsperada))) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoConDecimales);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-DECIMAL", resultado.getId());
        }
    }

    @Test
    @DisplayName("Debería configurar correctamente las URLs de retorno")
    void testCrearPreferencia_URLsRetornoCorrectas() throws MPException, MPApiException {
        // Arrange
        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-URLS");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> {
                         // Capturar el request para verificar las URLs
                         when(mock.create(any(PreferenceRequest.class))).thenAnswer(invocation -> {
                             PreferenceRequest request = invocation.getArgument(0);

                             // Verificar que el request tiene back URLs configuradas
                             assertNotNull(request.getBackUrls());
                             assertEquals("http://localhost:4200/pago-exitoso", request.getBackUrls().getSuccess());
                             assertEquals("http://localhost:4200/pago-fallo", request.getBackUrls().getFailure());
                             assertEquals("http://localhost:4200/pago-pendiente", request.getBackUrls().getPending());

                             return preferenciaEsperada;
                         });
                     })) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoDTO);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-URLS", resultado.getId());
        }
    }

    @Test
    @DisplayName("Debería configurar items con moneda COP")
    void testCrearPreferencia_MonedaCOP() throws MPException, MPApiException {
        // Arrange
        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-COP");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> {
                         when(mock.create(any(PreferenceRequest.class))).thenAnswer(invocation -> {
                             PreferenceRequest request = invocation.getArgument(0);

                             // Verificar que todos los items tienen currencyId = "COP"
                             assertNotNull(request.getItems());
                             request.getItems().forEach(item -> {
                                 assertEquals("COP", item.getCurrencyId());
                                 assertEquals(1, item.getQuantity());
                             });

                             return preferenciaEsperada;
                         });
                     })) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoDTO);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-COP", resultado.getId());
        }
    }

    @Test
    @DisplayName("Debería manejar carrito vacío sin lanzar excepción")
    void testCrearPreferencia_CarritoVacio() throws MPException, MPApiException {
        // Arrange
        PagoCarritoDTO carritoVacio = new PagoCarritoDTO();
        carritoVacio.setItems(new ArrayList<>());

        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-EMPTY");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> when(mock.create(any(PreferenceRequest.class))).thenReturn(preferenciaEsperada))) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carritoVacio);

            // Assert
            assertNotNull(resultado);
            assertEquals("MP-PREF-EMPTY", resultado.getId());
        }
    }

    @Test
    @DisplayName("Debería usar el access token proporcionado en el constructor")
    void testConstructor_AccessToken() {
        // Arrange
        String customToken = "CUSTOM_TOKEN_XYZ";

        // Act
        PagoService customService = new PagoService(customToken);

        // Assert
        assertNotNull(customService);
        // El access token se usa internamente en crearPreferencia
    }

    @Test
    @DisplayName("Debería mapear correctamente títulos y precios de items")
    void testCrearPreferencia_MapeoCorrecto() throws MPException, MPApiException {
        // Arrange
        ItemPagoDTO item = new ItemPagoDTO();
        item.setTitulo("Producto Test con Ñ y Tildes ÁÉÍ");
        item.setPrecio(new BigDecimal("12345.67"));

        PagoCarritoDTO carrito = new PagoCarritoDTO();
        carrito.setItems(List.of(item));

        Preference preferenciaEsperada = mock(Preference.class);
        when(preferenciaEsperada.getId()).thenReturn("MP-PREF-MAP");

        try (MockedStatic<MercadoPagoConfig> mockedConfig = mockStatic(MercadoPagoConfig.class);
             MockedConstruction<PreferenceClient> mockedClient = mockConstruction(PreferenceClient.class,
                     (mock, context) -> {
                         when(mock.create(any(PreferenceRequest.class))).thenAnswer(invocation -> {
                             PreferenceRequest request = invocation.getArgument(0);

                             // Verificar que el título se mapeó correctamente
                             assertEquals(1, request.getItems().size());
                             assertEquals("Producto Test con Ñ y Tildes ÁÉÍ", request.getItems().get(0).getTitle());
                             assertEquals(new BigDecimal("12345.67"), request.getItems().get(0).getUnitPrice());

                             return preferenciaEsperada;
                         });
                     })) {

            // Act
            Preference resultado = pagoService.crearPreferencia(carrito);

            // Assert
            assertNotNull(resultado);
        }
    }
}
