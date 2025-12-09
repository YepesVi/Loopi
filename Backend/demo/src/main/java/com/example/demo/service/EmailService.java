package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.HistorialCompra;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

@Service
public class EmailService {

    private final SendGrid sendGrid;

    public EmailService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    public void sendPurchaseEmail(String correoDestino, HistorialCompra historial) {

        Email from = new Email("tucorreo@tudominio.com"); // puede ser el que registraste en SendGrid
        String subject = "Confirmación de compra";
        Email to = new Email(correoDestino);

        String mensaje = "Hola! Tu compra fue realizada exitosamente.\n" +
            "ID de compra: " + historial.getId() + "\n" +
            "Fecha: " + historial.getFechaCompra() + "\n" +
            "Gracias por usar Loopi ♥";

        Content content = new Content("text/plain", mensaje);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            sendGrid.api(request);

        } catch (IOException ex) {
            throw new RuntimeException("Error enviando correo: " + ex.getMessage());
        }
    }
}
