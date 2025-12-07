package com.example.demo.service;

import com.example.demo.entity.HistorialCompra;
import com.example.demo.entity.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private HistorialCompra historialCompra;
    private List<Producto> productos;

    @BeforeEach
    void setUp() {
        // Crear productos de prueba
        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setTitulo("Laptop HP");
        producto1.setPrecio(1500.00);

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setTitulo("Mouse Logitech");
        producto2.setPrecio(25.50);

        productos = new ArrayList<>(Arrays.asList(producto1, producto2));

        // Crear historial de compra
        historialCompra = new HistorialCompra();
        historialCompra.setId(1L);
        historialCompra.setFechaCompra(LocalDateTime.of(2024, 1, 15, 10, 30));
        historialCompra.setProductos(productos);
    }


    @Test
    @DisplayName("Debería enviar email con información correcta del historial de compra")
    void testSendPurchaseEmail_Success() {
        // Arrange
        String correoDestino = "cliente@example.com";

        // Act
        emailService.sendPurchaseEmail(correoDestino, historialCompra);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertNotNull(sentMessage);
        assertEquals(correoDestino, sentMessage.getTo()[0]);
        assertEquals("Historial de Compra - Gracias por tu compra", sentMessage.getSubject());

        String messageText = sentMessage.getText();
        assertNotNull(messageText);
        assertTrue(messageText.contains("Loopi"));
        assertTrue(messageText.contains("!Gracias por tu compra!"));
        assertTrue(messageText.contains("Fecha de compra: 2024-01-15T10:30"));
        assertTrue(messageText.contains("Productos adquiridos:"));
        assertTrue(messageText.contains("Laptop HP"));
        assertTrue(messageText.contains("1500.0"));
        assertTrue(messageText.contains("Mouse Logitech"));
        assertTrue(messageText.contains("25.5"));
    }

    @Test
    @DisplayName("Debería enviar email con un solo producto")
    void testSendPurchaseEmail_SingleProduct() {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setTitulo("Teclado Mecánico");
        producto.setPrecio(89.99);

        historialCompra.setProductos(Arrays.asList(producto));
        String correoDestino = "usuario@test.com";

        // Act
        emailService.sendPurchaseEmail(correoDestino, historialCompra);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String messageText = sentMessage.getText();

        assertTrue(messageText.contains("Teclado Mecánico"));
        assertTrue(messageText.contains("89.99"));
    }

    @Test
    @DisplayName("Debería enviar email aunque la lista de productos esté vacía")
    void testSendPurchaseEmail_EmptyProductList() {
        // Arrange
        historialCompra.setProductos(Arrays.asList());
        String correoDestino = "cliente@example.com";

        // Act
        emailService.sendPurchaseEmail(correoDestino, historialCompra);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String messageText = sentMessage.getText();

        assertTrue(messageText.contains("Productos adquiridos:"));
        assertTrue(messageText.contains("Loopi"));
    }

    @Test
    @DisplayName("Debería propagar excepción cuando JavaMailSender falla")
    void testSendPurchaseEmail_MailSenderException() {
        // Arrange
        String correoDestino = "cliente@example.com";
        doThrow(new RuntimeException("Error al enviar email"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            emailService.sendPurchaseEmail(correoDestino, historialCompra);
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería formatear correctamente múltiples productos en el email")
    void testSendPurchaseEmail_MultipleProducts() {
        // Arrange
        Producto producto3 = new Producto();
        producto3.setId(3L);
        producto3.setTitulo("Monitor 27\"");
        producto3.setPrecio(350.75);

        historialCompra.getProductos().add(producto3);
        String correoDestino = "comprador@test.com";

        // Act
        emailService.sendPurchaseEmail(correoDestino, historialCompra);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        String messageText = sentMessage.getText();

        // Verificar que todos los productos están presentes
        assertTrue(messageText.contains("Laptop HP"));
        assertTrue(messageText.contains("Mouse Logitech"));
        assertTrue(messageText.contains("Monitor 27\""));

        // Verificar formato de lista
        assertTrue(messageText.contains("- Laptop HP | Valor: 1500.0"));
        assertTrue(messageText.contains("- Mouse Logitech | Valor: 25.5"));
        assertTrue(messageText.contains("- Monitor 27\" | Valor: 350.75"));
    }

    @Test
    @DisplayName("Debería incluir todos los elementos requeridos en el email")
    void testSendPurchaseEmail_RequiredElements() {
        // Arrange
        String correoDestino = "test@example.com";

        // Act
        emailService.sendPurchaseEmail(correoDestino, historialCompra);

        // Assert
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Verificar destinatario
        assertArrayEquals(new String[]{correoDestino}, sentMessage.getTo());

        // Verificar asunto
        assertNotNull(sentMessage.getSubject());
        assertFalse(sentMessage.getSubject().isEmpty());

        // Verificar cuerpo del mensaje
        assertNotNull(sentMessage.getText());
        assertFalse(sentMessage.getText().isEmpty());
    }
}

