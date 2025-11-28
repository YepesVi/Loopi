package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.HistorialCompra;
import com.example.demo.service.HistorialCompraService;

@RestController
@RequestMapping("/api/historial-compra")
@CrossOrigin(origins = "*")
public class HistorialCompraController {
    
    @Autowired
    private HistorialCompraService historialCompraService;

    @GetMapping("/{cedula}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable String cedula) {
        List<HistorialCompra> historial = historialCompraService.obtenerHistorial(cedula);

        if (historial.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontró historial para este usuario.");
        }

        return ResponseEntity.ok(historial);
    }
}
