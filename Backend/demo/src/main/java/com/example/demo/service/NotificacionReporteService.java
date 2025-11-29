package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.NotificacionReporteDTO;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductoRepository;

@Service
public class NotificacionReporteService {
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ProductoRepository productoRepository;

    public boolean sendReportEmail(NotificacionReporteDTO request) {
        Optional<Producto> productOpt = productoRepository.findById(request.getProductId());

        if (productOpt.isEmpty()) {
            System.out.println("❌ Producto no encontrado.");
            return false;
        }

        Producto producto = productOpt.get();
        User owner = producto.getPropietario(); // Asumiendo que product tiene relación ManyToOne con User

        if (owner == null || owner.getCorreo() == null) {
            System.out.println("❌ El producto no tiene dueño o no tiene correo.");
            return false;
        }

        String toEmail = owner.getCorreo().trim().toLowerCase();
        String subject = "Tu producto ha sido reportado";
        String body = buildEmailBody(producto, request);

        try {
            sendEmail(toEmail, subject, body);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Error enviando correo: " + e.getMessage());
            return false;
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }

    private String buildEmailBody(Producto product, NotificacionReporteDTO request) {
        return """
                Hola,

                Tu producto ha sido reportado.

                🛒 Producto: %s
                📩 Mensaje del reporte:
                %s

                Por favor revisa este reporte y toma las acciones necesarias.

                Gracias.
                """.formatted(
                product.getTitulo(),
                request.getReporterMessage()
        );
    }
}
