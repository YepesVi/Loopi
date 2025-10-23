package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
        @RequestParam String nombre,
        @RequestParam String apellido,
        @RequestParam String cedula,
        @RequestParam String telefono,
        @RequestParam String correo,
        @RequestParam String direccion,
        @RequestParam String password,
        @RequestParam(required = false) MultipartFile foto
    ) {
        try {
            User user = new User();
            user.setNombre(nombre);
            user.setApellido(apellido);
            user.setCedula(cedula);
            user.setTelefono(telefono);
            user.setCorreo(correo);
            user.setDireccion(direccion);
            user.setPassword(password); // En producción, encripta con BCrypt

            if (foto != null && !foto.isEmpty()) {
                String nombreArchivo = UUID.randomUUID() + "_" + foto.getOriginalFilename();
                Path ruta = Paths.get("uploads").resolve(nombreArchivo);
                Files.createDirectories(ruta.getParent());
                Files.copy(foto.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);
                user.setFotoUrl(nombreArchivo);
            }

            User registrado = userService.register(user);
            return ResponseEntity.ok(registrado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al registrar usuario");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Optional<User> found = userService.login(user.getCorreo(), user.getPassword());

            if (found.isPresent()) {
                return ResponseEntity.ok(found.get()); // ✅ Login exitoso
            } else {
                return ResponseEntity.status(401).body("Credenciales inválidas"); // ❌ Login fallido
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno en el servidor");
        }
    }
}
