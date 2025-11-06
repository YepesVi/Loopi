package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Almacén temporal de tokens
    private Map<String, String> tokenStore = new HashMap<>();

    // Registro de usuario
    public User register(User user) {
        return userRepository.save(user);
    }

    // Login por correo y contraseña
    public Optional<User> login(String correo, String password) {
        Optional<User> user = userRepository.findByCorreo(correo);
        return user.filter(u -> password == null || u.getPassword().equals(password));
    }

    // Validaciones opcionales
    public boolean correoExiste(String correo) {
        return userRepository.existsByCorreo(correo);
    }

    public boolean cedulaExiste(String cedula) {
        return userRepository.existsByCedula(cedula);
    }

    // 📧 Enviar correo de recuperación
    public boolean sendRecoveryEmail(String correo) {
    correo = correo.trim().toLowerCase();
    Optional<User> userOpt = userRepository.findByCorreo(correo);

    if (userOpt.isEmpty()) {
        System.out.println("⚠️ Correo no registrado: " + correo);
        return false; // ❌ No se envió
    }

    String token = UUID.randomUUID().toString();
    tokenStore.put(token, correo);

    String link = "http://localhost:4200/#/reset-password?token=" + token;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(correo);
    message.setSubject("Recuperación de contraseña");
    message.setText("Haz clic en el siguiente enlace para restablecer tu contraseña:\n" + link);
    mailSender.send(message);

    System.out.println("📧 Correo de recuperación enviado a: " + correo);
    return true; // ✅ Se envió
}

    // 🔒 Restablecer contraseña
   public boolean resetPasswordDirect(String correo, String cedula, String newPassword) {
    correo = correo.trim().toLowerCase();
    Optional<User> userOpt = userRepository.findByCorreo(correo);

    if (userOpt.isEmpty() || !userOpt.get().getCedula().equals(cedula)) {
        return false;
    }

    User user = userOpt.get();
    user.setPassword(newPassword);
    userRepository.save(user);
    return true;
}

public boolean updateUserProfile(String cedula, Map<String, String> updates) {
    Optional<User> userOpt = userRepository.findByCedula(cedula);

    if (userOpt.isEmpty()) {
        return false;
    }

    User user = userOpt.get();

    // Actualizar campos si están presentes
    if (updates.containsKey("nombre")) user.setNombre(updates.get("nombre"));
    if (updates.containsKey("apellido")) user.setApellido(updates.get("apellido"));
    if (updates.containsKey("telefono")) user.setTelefono(updates.get("telefono"));
    if (updates.containsKey("correo")) user.setCorreo(updates.get("correo"));
    if (updates.containsKey("direccion")) user.setDireccion(updates.get("direccion"));
    if (updates.containsKey("fotoUrl")) user.setFotoUrl(updates.get("fotoUrl"));
    if (updates.containsKey("password")) user.setPassword(updates.get("password")); // encripta si usas BCrypt

    userRepository.save(user);
    return true;
}


}