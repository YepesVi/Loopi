package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    // 📝 Registro con foto
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String cedula,
            @RequestParam String telefono,
            @RequestParam String correo,
            @RequestParam String direccion,
            @RequestParam String password,
            @RequestParam(required = false) MultipartFile foto) {
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

    // 🔐 Login por correo y contraseña
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

    // 📧 Solicitud de recuperación de contraseña
   @PostMapping("/recover")
public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> payload) {
    String correo = payload.get("correo");
    try {
        boolean enviado = userService.sendRecoveryEmail(correo);
        if (enviado) {
            return ResponseEntity.ok("Correo de recuperación enviado");
        } else {
            return ResponseEntity.badRequest().body("Correo no registrado");
        }
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error al enviar correo de recuperación");
    }
}


    // 🔒 Restablecer contraseña 
    @PostMapping("/reset-direct")
public ResponseEntity<?> resetPasswordDirect(@RequestBody Map<String, String> payload) {
    String correo = payload.get("correo");
    String cedula = payload.get("cedula");
    String newPassword = payload.get("newPassword");

    boolean actualizado = userService.resetPasswordDirect(correo, cedula, newPassword);

    if (!actualizado) {
        return ResponseEntity.badRequest().body("Datos inválidos para restablecer la contraseña");
    }

    return ResponseEntity.ok("Contraseña actualizada correctamente");
}

// ✏️ Editar perfil
@PutMapping("/update")
public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
    String cedula = body.get("cedula");
    boolean actualizado = userService.updateUserProfile(cedula, body);

    if (actualizado) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "mensaje", "✅ Tu perfil ha sido actualizado exitosamente."
        ));
    } else {
        return ResponseEntity.status(404).body(Map.of(
            "success", false,
            "mensaje", "❌ Usuario no encontrado. Verifica la cédula."
        ));
    }
}



}
