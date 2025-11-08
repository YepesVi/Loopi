package com.example.demo.controller;

import com.example.demo.entity.Historial;
import com.example.demo.entity.Producto;
import com.example.demo.repository.HistorialRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/historiales")
@CrossOrigin(origins = "*")
public class HistorialController {

    @Autowired
    private HistorialRepository historialRepo;

    @Autowired
    private ProductoRepository productoRepo;

    // ✅ Listar todo el historial (global)
    @GetMapping
    public ResponseEntity<List<Historial>> listarHistoriales() {
        return ResponseEntity.ok(historialRepo.findAll());
    }

    // ✅ Listar historial por producto
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<Historial>> listarPorProducto(@PathVariable Long productoId) {
        List<Historial> historial = historialRepo.findByProductoIdOrderByFechaRegistroDesc(productoId);
        return ResponseEntity.ok(historial);
    }

    // ✅ Crear un registro en el historial
    @PostMapping
    public ResponseEntity<?> registrarEvento(
            @RequestParam("productoId") Long productoId,
            @RequestParam("estado") String estado,
            @RequestParam("accion") String accion,
            @RequestParam("usuario") String usuario) {

        Optional<Producto> productoOpt = productoRepo.findById(productoId);
        if (productoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El producto no existe");
        }

        Historial historial = new Historial(
                productoOpt.get(),
                estado,
                accion,
                usuario
        );

        historialRepo.save(historial);
        return ResponseEntity.ok(historial);

        
    }

    // ✅ Eliminar un registro del historial
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarHistorial(@PathVariable Long id) {
        Optional<Historial> historialOpt = historialRepo.findById(id);
        if (historialOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registro no encontrado");
        }

        historialRepo.deleteById(id);
        return ResponseEntity.ok("Registro eliminado correctamente");
    }



}


