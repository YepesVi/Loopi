package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.NotificacionReporteDTO;
import com.example.demo.service.NotificacionReporteService;

@RestController
@RequestMapping("/api/notificacion-reporte")
@CrossOrigin(origins = "*")
public class NotificacionReporteController {

    @Autowired
    private NotificacionReporteService notificacionReporteService;

    @PostMapping("")
    public ResponseEntity<?> reportProduct(@RequestBody NotificacionReporteDTO request) {

        boolean sent = notificacionReporteService.sendReportEmail(request);
        System.out.println("📧 Reporte enviado: " + sent);
        if (!sent) {
            return ResponseEntity.status(500).body("No se pudo enviar el reporte.");
        }

        return ResponseEntity.ok(Map.of("message", "Reporte enviado correctamente."));

    }
}
