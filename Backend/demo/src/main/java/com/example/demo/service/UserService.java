package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Registro de usuario
    public User register(User user) {
        return userRepository.save(user);
    }

    // Login por correo y contraseña
    public Optional<User> login(String correo, String password) {
        Optional<User> user = userRepository.findByCorreo(correo);
        return user.filter(u -> u.getPassword().equals(password));
    }

    // Validaciones opcionales
    public boolean correoExiste(String correo) {
        return userRepository.existsByCorreo(correo);
    }

    public boolean cedulaExiste(String cedula) {
        return userRepository.existsByCedula(cedula);
    }
}
