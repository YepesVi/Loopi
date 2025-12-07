package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.demo.DTO.NotificacionReporteDTO;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionReporteService - Pruebas Unitarias")
class NotificacionReporteServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private NotificacionReporteService notificacionReporteService;

    private NotificacionReporteDTO reporteDTO;
    private Producto producto;
    private User propietario;

    @BeforeEach
    void setUp() {
        // Setup User (Propietario)
        propietario = new User();
        propietario.setId(1L);
        propietario.setCedula("123456789");
        propietario.setCorreo("propietario@example.com");
        propietario.setNombre("Juan");

        // Setup Producto
        producto = new Producto();
        producto.setId(1L);
        producto.setTitulo("Laptop HP");
        producto.setDescripcion("Laptop usada en buen estado");
        producto.setPrecio(1500000.0);
        producto.setPropietario(propietario);

        // Setup NotificacionReporteDTO
        reporteDTO = new NotificacionReporteDTO();
        reporteDTO.setProductId(1L);
        reporteDTO.setReporterMessage("Este producto tiene una descripción engañosa");
    }

    // ==================== Tests para sendReportEmail ====================

    @Test
    @DisplayName("Debería enviar correo de reporte exitosamente")
    void testSendReportEmail_Exitoso() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);
        verify(productoRepository).findById(1L);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("propietario@example.com", capturedMessage.getTo()[0]);
        assertEquals("Tu producto ha sido reportado", capturedMessage.getSubject());
        assertNotNull(capturedMessage.getText());
        assertTrue(capturedMessage.getText().contains("Laptop HP"));
        assertTrue(capturedMessage.getText().contains("Este producto tiene una descripción engañosa"));
    }

    @Test
    @DisplayName("Debería normalizar correo (trim y lowercase)")
    void testSendReportEmail_NormalizarCorreo() {
        // Arrange
        propietario.setCorreo("  PROPIETARIO@EXAMPLE.COM  ");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("propietario@example.com", capturedMessage.getTo()[0]);
    }

    @Test
    @DisplayName("Debería retornar false si producto no existe")
    void testSendReportEmail_ProductoNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());
        reporteDTO.setProductId(999L);

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).findById(999L);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería retornar false si producto no tiene propietario")
    void testSendReportEmail_SinPropietario() {
        // Arrange
        producto.setPropietario(null);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).findById(1L);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería retornar false si propietario no tiene correo")
    void testSendReportEmail_PropietarioSinCorreo() {
        // Arrange
        propietario.setCorreo(null);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).findById(1L);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería retornar false si falla el envío del correo")
    void testSendReportEmail_ErrorEnvio() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new MailException("Error de conexión") {}).when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertFalse(resultado);
        verify(productoRepository).findById(1L);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería incluir título del producto en el correo")
    void testSendReportEmail_ContieneInformacionProducto() {
        // Arrange
        producto.setTitulo("iPhone 13 Pro Max");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains("iPhone 13 Pro Max"));
    }

    @Test
    @DisplayName("Debería incluir mensaje del reporte en el correo")
    void testSendReportEmail_ContieneMensajeReporte() {
        // Arrange
        reporteDTO.setReporterMessage("El producto está en peor estado del descrito");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains("El producto está en peor estado del descrito"));
    }

    @Test
    @DisplayName("Debería tener subject correcto")
    void testSendReportEmail_SubjectCorrecto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("Tu producto ha sido reportado", capturedMessage.getSubject());
    }

    @Test
    @DisplayName("Debería incluir firma de Loopi Team en el correo")
    void testSendReportEmail_ContieneFirma() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains("Loopi Team"));
    }

    @Test
    @DisplayName("Debería manejar mensajes de reporte largos")
    void testSendReportEmail_MensajeLargo() {
        // Arrange
        String mensajeLargo = "Este es un mensaje muy largo que describe en detalle todos los problemas encontrados en el producto. ".repeat(10);
        reporteDTO.setReporterMessage(mensajeLargo);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains(mensajeLargo));
    }

    @Test
    @DisplayName("Debería manejar caracteres especiales en el mensaje")
    void testSendReportEmail_CaracteresEspeciales() {
        // Arrange
        reporteDTO.setReporterMessage("Producto con ñ, tildes áéíóú y símbolos @#$%");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains("ñ, tildes áéíóú y símbolos @#$%"));
    }

    @Test
    @DisplayName("Debería manejar correos con diferentes dominios")
    void testSendReportEmail_DiferentesDominios() {
        // Arrange
        propietario.setCorreo("usuario@gmail.com");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        assertEquals("usuario@gmail.com", messageCaptor.getValue().getTo()[0]);
    }

    @Test
    @DisplayName("Debería manejar RuntimeException genérica al enviar correo")
    void testSendReportEmail_RuntimeException() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doThrow(new RuntimeException("Error inesperado")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertFalse(resultado);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería formatear correctamente el cuerpo del correo")
    void testSendReportEmail_FormatoCuerpoCorreo() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String body = messageCaptor.getValue().getText();
        assertTrue(body.contains("Hola,"));
        assertTrue(body.contains("Tu producto ha sido reportado"));
        assertTrue(body.contains("🛒 Producto:"));
        assertTrue(body.contains("📩 Mensaje del reporte:"));
        assertTrue(body.contains("Por favor revisa este reporte"));
        assertTrue(body.contains("Gracias."));
        assertTrue(body.contains("Loopi Team"));
    }

    @Test
    @DisplayName("Debería manejar correos con espacios al inicio y final")
    void testSendReportEmail_CorreoConEspacios() {
        // Arrange
        propietario.setCorreo("   test@example.com   ");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        // Verificar que se eliminaron los espacios
        assertEquals("test@example.com", messageCaptor.getValue().getTo()[0]);
    }

    @Test
    @DisplayName("Debería manejar títulos de producto con caracteres especiales")
    void testSendReportEmail_TituloConCaracteresEspeciales() {
        // Arrange
        producto.setTitulo("Laptop HP™ ProBook® 450 G8 (Reacondicionado)");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        assertTrue(resultado);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        assertTrue(messageCaptor.getValue().getText().contains("Laptop HP™ ProBook® 450 G8 (Reacondicionado)"));
    }

    @Test
    @DisplayName("Debería enviar solo un correo por reporte")
    void testSendReportEmail_UnSoloCorreo() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificacionReporteService.sendReportEmail(reporteDTO);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
