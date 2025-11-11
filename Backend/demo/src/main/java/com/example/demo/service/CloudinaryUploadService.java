package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    /**
     * Sube un archivo a Cloudinary en una carpeta específica.
     *
     * @param file El archivo a subir.
     * @param folderName El nombre de la carpeta en Cloudinary (ej. "productos", "avatares").
     * @return Un Map que contiene "secure_url" y "public_id".
     */
    public Map<String, String> uploadFile(MultipartFile file, String folderName) throws IOException {
        
        // 🌟 AÑADIR CARPETA Y un ID público único
        Map<String, Object> params = new HashMap<>();
        params.put("folder", "loopi_project/" + folderName);
        params.put("public_id", UUID.randomUUID().toString());
        params.put("overwrite", true);

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        return Map.of(
            "secure_url", (String) uploadResult.get("secure_url"),
            "public_id", (String) uploadResult.get("public_id")
        );
    }

    /**
     * 🌟 NUEVO: Elimina un archivo de Cloudinary usando su public_id.
     */
    public void deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isBlank()) {
            return; // No hacer nada si no hay ID
        }
        // Llama a la API 'destroy' de Cloudinary
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}