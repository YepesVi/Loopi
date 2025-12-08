package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CloudinaryUploadService cloudinaryUploadService;

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("Debería subir archivo correctamente a Cloudinary y retornar URL y public_id")
    void uploadFile_DeberiaSubirArchivoCorrectamente() throws IOException {
        // Arrange
        String folderName = "productos";
        byte[] fileBytes = "test content".getBytes();
        String expectedUrl = "https://res.cloudinary.com/test/image/upload/v123/loopi_project/productos/abc123.jpg";
        String expectedPublicId = "loopi_project/productos/abc123";

        when(file.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", expectedUrl);
        uploadResult.put("public_id", expectedPublicId);

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // Act
        Map<String, String> result = cloudinaryUploadService.uploadFile(file, folderName);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result.get("secure_url"));
        assertEquals(expectedPublicId, result.get("public_id"));

        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(eq(fileBytes), anyMap());
    }

    @Test
    @DisplayName("Debería incluir la carpeta correcta en los parámetros de subida")
    void uploadFile_DeberiaIncluirCarpetaEnParametros() throws IOException {
        // Arrange
        String folderName = "avatares";
        byte[] fileBytes = "avatar data".getBytes();

        when(file.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");
        uploadResult.put("public_id", "loopi_project/avatares/xyz789");

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(uploader.upload(any(byte[].class), paramsCaptor.capture())).thenReturn(uploadResult);

        // Act
        cloudinaryUploadService.uploadFile(file, folderName);

        // Assert
        Map<String, Object> capturedParams = paramsCaptor.getValue();
        assertEquals("loopi_project/" + folderName, capturedParams.get("folder"));
        assertTrue(capturedParams.containsKey("public_id"));
        assertEquals(true, capturedParams.get("overwrite"));
    }

    @Test
    @DisplayName("Debería generar un public_id único para cada archivo subido")
    void uploadFile_DeberiaGenerarPublicIdUnico() throws IOException {
        // Arrange
        String folderName = "productos";
        byte[] fileBytes = "content".getBytes();

        when(file.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");
        uploadResult.put("public_id", "test_id");

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(uploader.upload(any(byte[].class), paramsCaptor.capture())).thenReturn(uploadResult);

        // Act
        cloudinaryUploadService.uploadFile(file, folderName);
        cloudinaryUploadService.uploadFile(file, folderName);

        // Assert
        Map<String, Object> params1 = paramsCaptor.getAllValues().get(0);
        Map<String, Object> params2 = paramsCaptor.getAllValues().get(1);

        assertNotNull(params1.get("public_id"));
        assertNotNull(params2.get("public_id"));
        assertNotEquals(params1.get("public_id"), params2.get("public_id"));
    }

    @Test
    @DisplayName("Debería lanzar IOException cuando falla la lectura del archivo")
    void uploadFile_DeberiaLanzarIOExceptionCuandoFalla() throws IOException {
        // Arrange
        String folderName = "productos";
        when(file.getBytes()).thenThrow(new IOException("Error reading file"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            cloudinaryUploadService.uploadFile(file, folderName);
        });
    }

    @Test
    @DisplayName("Debería eliminar archivo de Cloudinary correctamente usando public_id")
    void deleteFile_DeberiaEliminarArchivoCorrectamente() throws IOException {
        // Arrange
        String publicId = "loopi_project/productos/abc123";
        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "ok");

        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(deleteResult);

        // Act
        cloudinaryUploadService.deleteFile(publicId);

        // Assert
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).destroy(eq(publicId), anyMap());
    }

    @Test
    @DisplayName("No debería hacer nada cuando el public_id es nulo")
    void deleteFile_NoDeberiaHacerNadaConPublicIdNulo() throws IOException {
        // Act
        cloudinaryUploadService.deleteFile(null);

        // Assert
        verify(cloudinary, never()).uploader();
        verify(uploader, never()).destroy(anyString(), anyMap());
    }

    @Test
    @DisplayName("No debería hacer nada cuando el public_id está vacío o en blanco")
    void deleteFile_NoDeberiaHacerNadaConPublicIdVacio() throws IOException {
        // Act
        cloudinaryUploadService.deleteFile("");
        cloudinaryUploadService.deleteFile("   ");

        // Assert
        verify(cloudinary, never()).uploader();
        verify(uploader, never()).destroy(anyString(), anyMap());
    }

    @Test
    @DisplayName("Debería lanzar IOException cuando falla la eliminación del archivo")
    void deleteFile_DeberiaLanzarIOExceptionCuandoFalla() throws IOException {
        // Arrange
        String publicId = "loopi_project/productos/abc123";
        when(uploader.destroy(eq(publicId), anyMap()))
                .thenThrow(new IOException("Error deleting file"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            cloudinaryUploadService.deleteFile(publicId);
        });
    }

    @Test
    @DisplayName("Debería retornar Map con solo las claves secure_url y public_id")
    void uploadFile_DeberiaRetornarMapConClavesSoloSecureUrlYPublicId() throws IOException {
        // Arrange
        String folderName = "test";
        byte[] fileBytes = "content".getBytes();

        when(file.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");
        uploadResult.put("public_id", "test_id");
        uploadResult.put("other_field", "other_value");

        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        // Act
        Map<String, String> result = cloudinaryUploadService.uploadFile(file, folderName);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("secure_url"));
        assertTrue(result.containsKey("public_id"));
        assertFalse(result.containsKey("other_field"));
    }
}