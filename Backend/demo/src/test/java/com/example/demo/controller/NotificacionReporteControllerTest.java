package com.example.demo.controller;

import com.example.demo.DTO.NotificacionReporteDTO;
import com.example.demo.service.NotificacionReporteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacionReporteController.class)
@DisplayName("NotificacionReporteController Tests")
class NotificacionReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacionReporteService notificacionReporteService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificacionReporteDTO notificacionReporteDTO;

    @BeforeEach
    void setUp() {
        notificacionReporteDTO = new NotificacionReporteDTO();
        // Configura los campos según tu DTO
        // Ejemplo:
        // notificacionReporteDTO.setProductoId(1L);
        // notificacionReporteDTO.setMotivo("Producto defectuoso");
        // notificacionReporteDTO.setDescripcion("El producto llegó dañado");
    }

    @Test
    @DisplayName("POST /api/notificacion-reporte - Debería enviar reporte exitosamente")
    void testReportProduct_Success() throws Exception {
        // Arrange
        when(notificacionReporteService.sendReportEmail(any(NotificacionReporteDTO.class)))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/notificacion-reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionReporteDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Reporte enviado correctamente."));

        verify(notificacionReporteService, times(1)).sendReportEmail(any(NotificacionReporteDTO.class));
    }

    @Test
    @DisplayName("POST /api/notificacion-reporte - Debería retornar error 500 cuando falla el envío")
    void testReportProduct_Failure() throws Exception {
        // Arrange
        when(notificacionReporteService.sendReportEmail(any(NotificacionReporteDTO.class)))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/notificacion-reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionReporteDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No se pudo enviar el reporte."));

        verify(notificacionReporteService, times(1)).sendReportEmail(any(NotificacionReporteDTO.class));
    }

    @Test
    @DisplayName("POST /api/notificacion-reporte - Debería manejar excepción del servicio")
    void testReportProduct_ServiceException() throws Exception {
        // Arrange
        when(notificacionReporteService.sendReportEmail(any(NotificacionReporteDTO.class)))
                .thenThrow(new RuntimeException("Error al enviar email"));

        // Act & Assert
        mockMvc.perform(post("/api/notificacion-reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificacionReporteDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error al enviar email")));

        verify(notificacionReporteService, times(1)).sendReportEmail(any(NotificacionReporteDTO.class));
    }

    @Test
    @DisplayName("POST /api/notificacion-reporte - Debería manejar request con campos nulos")
    void testReportProduct_CamposNulos() throws Exception {
        // Arrange
        NotificacionReporteDTO dtoVacio = new NotificacionReporteDTO();
        // productId y reporterMessage son null

        when(notificacionReporteService.sendReportEmail(any(NotificacionReporteDTO.class)))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/notificacion-reporte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoVacio)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("No se pudo enviar el reporte."));

        verify(notificacionReporteService, times(1)).sendReportEmail(any(NotificacionReporteDTO.class));
    }

}

