package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setNombre("Juan");
        testUser.setApellido("Pérez");
        testUser.setCedula("123456789");
        testUser.setTelefono("0987654321");
        testUser.setCorreo("juan@example.com");
        testUser.setDireccion("Calle Principal 123");
        testUser.setPassword("password123");
    }

    // ========== PRUEBAS DE REGISTRO ==========

    @Test
    @DisplayName("POST /api/auth/register - Debería registrar usuario sin foto")
    void testRegister_SinFoto_Success() throws Exception {
        // Arrange
        when(userService.register(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/register")
                        .param("nombre", "Juan")
                        .param("apellido", "Pérez")
                        .param("cedula", "123456789")
                        .param("telefono", "0987654321")
                        .param("correo", "juan@example.com")
                        .param("direccion", "Calle Principal 123")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.cedula").value("123456789"));

        verify(userService, times(1)).register(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Debería registrar usuario con foto")
    void testRegister_ConFoto_Success() throws Exception {
        // Arrange
        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        when(userService.register(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/register")
                        .file(foto)
                        .param("nombre", "Juan")
                        .param("apellido", "Pérez")
                        .param("cedula", "123456789")
                        .param("telefono", "0987654321")
                        .param("correo", "juan@example.com")
                        .param("direccion", "Calle Principal 123")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));

        verify(userService, times(1)).register(any(User.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Debería manejar error de registro")
    void testRegister_Error() throws Exception {
        // Arrange
        when(userService.register(any(User.class)))
                .thenThrow(new RuntimeException("Error en base de datos"));

        // Act & Assert
        mockMvc.perform(multipart("/api/auth/register")
                        .param("nombre", "Juan")
                        .param("apellido", "Pérez")
                        .param("cedula", "123456789")
                        .param("telefono", "0987654321")
                        .param("correo", "juan@example.com")
                        .param("direccion", "Calle Principal 123")
                        .param("password", "password123"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al registrar usuario"));

        verify(userService, times(1)).register(any(User.class));
    }

    // ========== PRUEBAS DE LOGIN ==========

    @Test
    @DisplayName("POST /api/auth/login - Debería hacer login exitoso")
    void testLogin_Success() throws Exception {
        // Arrange
        when(userService.login("juan@example.com", "password123"))
                .thenReturn(Optional.of(testUser));

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("correo", "juan@example.com");
        loginRequest.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("juan@example.com"))
                .andExpect(jsonPath("$.nombre").value("Juan"));

        verify(userService, times(1)).login("juan@example.com", "password123");
    }

    @Test
    @DisplayName("POST /api/auth/login - Debería rechazar credenciales inválidas")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        when(userService.login(anyString(), anyString()))
                .thenReturn(Optional.empty());

        User loginUser = new User();
        loginUser.setCorreo("juan@example.com");
        loginUser.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));

        verify(userService, times(1)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/login - Debería manejar error interno")
    void testLogin_InternalError() throws Exception {
        // Arrange
        when(userService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error de conexión"));

        User loginUser = new User();
        loginUser.setCorreo("juan@example.com");
        loginUser.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error interno en el servidor"));

        verify(userService, times(1)).login(anyString(), anyString());
    }

    // ========== PRUEBAS DE RECUPERACIÓN ==========

    @Test
    @DisplayName("POST /api/auth/recover - Debería enviar correo de recuperación")
    void testRecoverPassword_Success() throws Exception {
        // Arrange
        when(userService.sendRecoveryEmail("juan@example.com")).thenReturn(true);

        Map<String, String> payload = new HashMap<>();
        payload.put("correo", "juan@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Correo de recuperación enviado"));

        verify(userService, times(1)).sendRecoveryEmail("juan@example.com");
    }

    @Test
    @DisplayName("POST /api/auth/recover - Debería rechazar correo no registrado")
    void testRecoverPassword_EmailNotFound() throws Exception {
        // Arrange
        when(userService.sendRecoveryEmail(anyString())).thenReturn(false);

        Map<String, String> payload = new HashMap<>();
        payload.put("correo", "noexiste@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Correo no registrado"));

        verify(userService, times(1)).sendRecoveryEmail(anyString());
    }

    @Test
    @DisplayName("POST /api/auth/recover - Debería manejar error al enviar correo")
    void testRecoverPassword_Error() throws Exception {
        // Arrange
        when(userService.sendRecoveryEmail(anyString()))
                .thenThrow(new RuntimeException("Error de email"));

        Map<String, String> payload = new HashMap<>();
        payload.put("correo", "juan@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al enviar correo de recuperación"));

        verify(userService, times(1)).sendRecoveryEmail(anyString());
    }

    // ========== PRUEBAS DE RESET PASSWORD ==========

    @Test
    @DisplayName("POST /api/auth/reset-direct - Debería resetear contraseña exitosamente")
    void testResetPasswordDirect_Success() throws Exception {
        // Arrange
        when(userService.resetPasswordDirect("juan@example.com", "123456789", "newpass123"))
                .thenReturn(true);

        Map<String, String> payload = new HashMap<>();
        payload.put("correo", "juan@example.com");
        payload.put("cedula", "123456789");
        payload.put("newPassword", "newpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada correctamente"));

        verify(userService, times(1))
                .resetPasswordDirect("juan@example.com", "123456789", "newpass123");
    }

    @Test
    @DisplayName("POST /api/auth/reset-direct - Debería rechazar datos inválidos")
    void testResetPasswordDirect_InvalidData() throws Exception {
        // Arrange
        when(userService.resetPasswordDirect(anyString(), anyString(), anyString()))
                .thenReturn(false);

        Map<String, String> payload = new HashMap<>();
        payload.put("correo", "juan@example.com");
        payload.put("cedula", "999999999");
        payload.put("newPassword", "newpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Datos inválidos para restablecer la contraseña"));

        verify(userService, times(1))
                .resetPasswordDirect(anyString(), anyString(), anyString());
    }

    // ========== PRUEBAS DE UPDATE PROFILE ==========

    @Test
    @DisplayName("PUT /api/auth/update - Debería actualizar perfil exitosamente")
    void testUpdateProfile_Success() throws Exception {
        // Arrange
        when(userService.updateUserProfile(eq("123456789"), anyMap())).thenReturn(true);

        Map<String, String> payload = new HashMap<>();
        payload.put("cedula", "123456789");
        payload.put("nombre", "Juan Actualizado");
        payload.put("telefono", "0999999999");

        // Act & Assert
        mockMvc.perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.mensaje").value("✅ Tu perfil ha sido actualizado exitosamente."));

        verify(userService, times(1)).updateUserProfile(eq("123456789"), anyMap());
    }

    @Test
    @DisplayName("PUT /api/auth/update - Debería rechazar usuario no encontrado")
    void testUpdateProfile_UserNotFound() throws Exception {
        // Arrange
        when(userService.updateUserProfile(anyString(), anyMap())).thenReturn(false);

        Map<String, String> payload = new HashMap<>();
        payload.put("cedula", "999999999");
        payload.put("nombre", "Juan Actualizado");

        // Act & Assert
        mockMvc.perform(put("/api/auth/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.mensaje").value("❌ Usuario no encontrado. Verifica la cédula."));

        verify(userService, times(1)).updateUserProfile(anyString(), anyMap());
    }
}

