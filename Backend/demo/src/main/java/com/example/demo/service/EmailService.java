package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.entity.HistorialCompra;
import com.example.demo.entity.Producto;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPurchaseEmail(String correoDestino, HistorialCompra historial) {

        StringBuilder sb = new StringBuilder();
        sb.append("Loopi\n\n");
        sb.append("!Gracias por tu compra!\n\n");
        sb.append("Fecha de compra: ").append(historial.getFechaCompra()).append("\n\n");

        sb.append("Productos adquiridos:\n");

        for (Producto p : historial.getProductos()) {
            sb.append("- ").append(p.getTitulo())
              .append(" | Valor: ").append(p.getPrecio())
              .append("\n\n");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(correoDestino);
        message.setSubject("Historial de Compra - Gracias por tu compra");
        message.setText(sb.toString());

        mailSender.send(message);
    }

}
