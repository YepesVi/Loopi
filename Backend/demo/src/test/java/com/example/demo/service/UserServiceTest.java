package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Pruebas Unitarias")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserService userService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setCedula("123456789");
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setCorreo("juan@example.com");
        usuario.setPassword("password123");
        usuario.setTelefono("3001234567");
        usuario.setDireccion("Calle 123");
        usuario.setFotoUrl("http://example.com/foto.jpg");
    }

    // ==================== Tests para register ====================

    @Test
    @DisplayName("Debería registrar usuario exitosamente")
    void testRegister_Exitoso() {
        // Arrange
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        User resultado = userService.register(usuario);

        // Assert
        assertNotNull(resultado);
        assertEquals("123456789", resultado.getCedula());
        assertEquals("juan@example.com", resultado.getCorreo());
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería registrar usuario con campos mínimos")
    void testRegister_CamposMinimos() {
        // Arrange
        User usuarioMinimo = new User();
        usuarioMinimo.setCedula("987654321");
        usuarioMinimo.setCorreo("test@example.com");

        when(userRepository.save(usuarioMinimo)).thenReturn(usuarioMinimo);

        // Act
        User resultado = userService.register(usuarioMinimo);

        // Assert
        assertNotNull(resultado);
        assertEquals("987654321", resultado.getCedula());
        verify(userRepository).save(usuarioMinimo);
    }

    // ==================== Tests para login ====================

    @Test
    @DisplayName("Debería hacer login exitoso con credenciales correctas")
    void testLogin_Exitoso() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));

        // Act
        Optional<User> resultado = userService.login("juan@example.com", "password123");

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals("juan@example.com", resultado.get().getCorreo());
        verify(userRepository).findByCorreo("juan@example.com");
    }

    @Test
    @DisplayName("Debería fallar login con contraseña incorrecta")
    void testLogin_PasswordIncorrecta() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));

        // Act
        Optional<User> resultado = userService.login("juan@example.com", "wrongpassword");

        // Assert
        assertFalse(resultado.isPresent());
        verify(userRepository).findByCorreo("juan@example.com");
    }

    @Test
    @DisplayName("Debería fallar login con correo no existente")
    void testLogin_CorreoNoExiste() {
        // Arrange
        when(userRepository.findByCorreo("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> resultado = userService.login("noexiste@example.com", "password123");

        // Assert
        assertFalse(resultado.isPresent());
        verify(userRepository).findByCorreo("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería hacer login con password null")
    void testLogin_PasswordNull() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));

        // Act
        Optional<User> resultado = userService.login("juan@example.com", null);

        // Assert
        assertTrue(resultado.isPresent());
        verify(userRepository).findByCorreo("juan@example.com");
    }

    // ==================== Tests para correoExiste ====================

    @Test
    @DisplayName("Debería retornar true si correo existe")
    void testCorreoExiste_True() {
        // Arrange
        when(userRepository.existsByCorreo("juan@example.com")).thenReturn(true);

        // Act
        boolean resultado = userService.correoExiste("juan@example.com");

        // Assert
        assertTrue(resultado);
        verify(userRepository).existsByCorreo("juan@example.com");
    }

    @Test
    @DisplayName("Debería retornar false si correo no existe")
    void testCorreoExiste_False() {
        // Arrange
        when(userRepository.existsByCorreo("noexiste@example.com")).thenReturn(false);

        // Act
        boolean resultado = userService.correoExiste("noexiste@example.com");

        // Assert
        assertFalse(resultado);
        verify(userRepository).existsByCorreo("noexiste@example.com");
    }

    // ==================== Tests para cedulaExiste ====================

    @Test
    @DisplayName("Debería retornar true si cédula existe")
    void testCedulaExiste_True() {
        // Arrange
        when(userRepository.existsByCedula("123456789")).thenReturn(true);

        // Act
        boolean resultado = userService.cedulaExiste("123456789");

        // Assert
        assertTrue(resultado);
        verify(userRepository).existsByCedula("123456789");
    }

    @Test
    @DisplayName("Debería retornar false si cédula no existe")
    void testCedulaExiste_False() {
        // Arrange
        when(userRepository.existsByCedula("999999999")).thenReturn(false);

        // Act
        boolean resultado = userService.cedulaExiste("999999999");

        // Assert
        assertFalse(resultado);
        verify(userRepository).existsByCedula("999999999");
    }

    // ==================== Tests para sendRecoveryEmail ====================

    @Test
    @DisplayName("Debería enviar correo de recuperación exitosamente")
    void testSendRecoveryEmail_Exitoso() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = userService.sendRecoveryEmail("juan@example.com");

        // Assert
        assertTrue(resultado);
        verify(userRepository).findByCorreo("juan@example.com");

        // Verificar que el correo se envió con los datos correctos
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("juan@example.com", capturedMessage.getTo()[0]);
        assertEquals("Recuperación de contraseña", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains("http://localhost:4200/#/reset-password?token="));
    }

    @Test
    @DisplayName("Debería normalizar correo (trim y lowercase) al enviar recuperación")
    void testSendRecoveryEmail_NormalizarCorreo() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        boolean resultado = userService.sendRecoveryEmail("  JUAN@EXAMPLE.COM  ");

        // Assert
        assertTrue(resultado);
        verify(userRepository).findByCorreo("juan@example.com");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería retornar false si correo no está registrado")
    void testSendRecoveryEmail_CorreoNoRegistrado() {
        // Arrange
        when(userRepository.findByCorreo("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        boolean resultado = userService.sendRecoveryEmail("noexiste@example.com");

        // Assert
        assertFalse(resultado);
        verify(userRepository).findByCorreo("noexiste@example.com");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debería generar token único en cada envío")
    void testSendRecoveryEmail_TokenUnico() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        userService.sendRecoveryEmail("juan@example.com");
        userService.sendRecoveryEmail("juan@example.com");

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        String token1 = extractTokenFromMessage(messageCaptor.getAllValues().get(0));
        String token2 = extractTokenFromMessage(messageCaptor.getAllValues().get(1));

        assertNotEquals(token1, token2);
    }

    // Método auxiliar para extraer token del mensaje
    private String extractTokenFromMessage(SimpleMailMessage message) {
        String text = message.getText();
        int tokenStart = text.indexOf("token=") + 6;
        return text.substring(tokenStart);
    }

    // ==================== Tests para resetPasswordDirect ====================

    @Test
    @DisplayName("Debería restablecer contraseña exitosamente")
    void testResetPasswordDirect_Exitoso() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.resetPasswordDirect("juan@example.com", "123456789", "newPassword123");

        // Assert
        assertTrue(resultado);
        verify(userRepository).findByCorreo("juan@example.com");
        verify(userRepository).save(usuario);
        assertEquals("newPassword123", usuario.getPassword());
    }

    @Test
    @DisplayName("Debería normalizar correo al restablecer contraseña")
    void testResetPasswordDirect_NormalizarCorreo() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.resetPasswordDirect("  JUAN@EXAMPLE.COM  ", "123456789", "newPassword");

        // Assert
        assertTrue(resultado);
        verify(userRepository).findByCorreo("juan@example.com");
    }

    @Test
    @DisplayName("Debería fallar si correo no existe")
    void testResetPasswordDirect_CorreoNoExiste() {
        // Arrange
        when(userRepository.findByCorreo("noexiste@example.com")).thenReturn(Optional.empty());

        // Act
        boolean resultado = userService.resetPasswordDirect("noexiste@example.com", "123456789", "newPassword");

        // Assert
        assertFalse(resultado);
        verify(userRepository).findByCorreo("noexiste@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería fallar si cédula no coincide")
    void testResetPasswordDirect_CedulaNoCoincide() {
        // Arrange
        when(userRepository.findByCorreo("juan@example.com")).thenReturn(Optional.of(usuario));

        // Act
        boolean resultado = userService.resetPasswordDirect("juan@example.com", "wrongCedula", "newPassword");

        // Assert
        assertFalse(resultado);
        verify(userRepository).findByCorreo("juan@example.com");
        verify(userRepository, never()).save(any());
    }

    // ==================== Tests para updateUserProfile ====================

    @Test
    @DisplayName("Debería actualizar todos los campos del perfil")
    void testUpdateUserProfile_TodosLosCampos() {
        // Arrange
        Map<String, String> updates = new HashMap<>();
        updates.put("nombre", "Carlos");
        updates.put("apellido", "González");
        updates.put("telefono", "3009876543");
        updates.put("correo", "carlos@example.com");
        updates.put("direccion", "Calle 456");
        updates.put("fotoUrl", "http://example.com/nueva-foto.jpg");
        updates.put("password", "newPassword456");

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.updateUserProfile("123456789", updates);

        // Assert
        assertTrue(resultado);
        assertEquals("Carlos", usuario.getNombre());
        assertEquals("González", usuario.getApellido());
        assertEquals("3009876543", usuario.getTelefono());
        assertEquals("carlos@example.com", usuario.getCorreo());
        assertEquals("Calle 456", usuario.getDireccion());
        assertEquals("http://example.com/nueva-foto.jpg", usuario.getFotoUrl());
        assertEquals("newPassword456", usuario.getPassword());
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería actualizar solo campos específicos")
    void testUpdateUserProfile_CamposEspecificos() {
        // Arrange
        Map<String, String> updates = new HashMap<>();
        updates.put("nombre", "Pedro");
        updates.put("telefono", "3111111111");

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.updateUserProfile("123456789", updates);

        // Assert
        assertTrue(resultado);
        assertEquals("Pedro", usuario.getNombre());
        assertEquals("3111111111", usuario.getTelefono());
        // Campos no actualizados deben mantener valores originales
        assertEquals("Pérez", usuario.getApellido());
        assertEquals("juan@example.com", usuario.getCorreo());
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería manejar mapa vacío de actualizaciones")
    void testUpdateUserProfile_MapaVacio() {
        // Arrange
        Map<String, String> updates = new HashMap<>();

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.updateUserProfile("123456789", updates);

        // Assert
        assertTrue(resultado);
        // Todos los campos deben mantener valores originales
        assertEquals("Juan", usuario.getNombre());
        assertEquals("Pérez", usuario.getApellido());
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería fallar si usuario no existe")
    void testUpdateUserProfile_UsuarioNoExiste() {
        // Arrange
        Map<String, String> updates = new HashMap<>();
        updates.put("nombre", "Nuevo Nombre");

        when(userRepository.findByCedula("999999999")).thenReturn(Optional.empty());

        // Act
        boolean resultado = userService.updateUserProfile("999999999", updates);

        // Assert
        assertFalse(resultado);
        verify(userRepository).findByCedula("999999999");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar solo password")
    void testUpdateUserProfile_SoloPassword() {
        // Arrange
        Map<String, String> updates = new HashMap<>();
        updates.put("password", "superSecurePassword");

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.updateUserProfile("123456789", updates);

        // Assert
        assertTrue(resultado);
        assertEquals("superSecurePassword", usuario.getPassword());
        verify(userRepository).save(usuario);
    }

    @Test
    @DisplayName("Debería ignorar campos no reconocidos")
    void testUpdateUserProfile_CamposNoReconocidos() {
        // Arrange
        Map<String, String> updates = new HashMap<>();
        updates.put("nombre", "María");
        updates.put("campoInexistente", "valor"); // Este debe ser ignorado

        when(userRepository.findByCedula("123456789")).thenReturn(Optional.of(usuario));
        when(userRepository.save(usuario)).thenReturn(usuario);

        // Act
        boolean resultado = userService.updateUserProfile("123456789", updates);

        // Assert
        assertTrue(resultado);
        assertEquals("María", usuario.getNombre());
        verify(userRepository).save(usuario);
    }
}
