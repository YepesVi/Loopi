package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.PagoCarritoDTO;
import com.example.demo.service.PagoService;
import com.mercadopago.resources.preference.Preference;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pago")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearPreferencia(@RequestBody PagoCarritoDTO carritoDTO) {
        try {
            Preference preference = pagoService.crearPreferencia(carritoDTO);

            return ResponseEntity.ok().body(preference);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la preferencia: " + e.getMessage());
        }
    }
}
