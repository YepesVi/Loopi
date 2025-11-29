package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.HistorialCompra;
import com.example.demo.service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
@CrossOrigin(origins = "*")
public class CarritoController {
    @Autowired
    private CarritoService carritoService;

    @GetMapping("/{userId}")
    public ResponseEntity<Carrito> obtenerCarrito(@PathVariable String userId) {
        Carrito carrito = carritoService.getCarritoDeUsuario(userId);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/crear/{userId}")
    public ResponseEntity<Carrito> crearCarrito(@PathVariable String userId) {
        Carrito carrito = carritoService.crearCarritoParaUsuario(userId);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/agregar/{userId}/{productoId}")
    public ResponseEntity<Carrito> agregarProducto(
        @PathVariable String userId,
        @PathVariable Long productoId
    ) {
        Carrito carrito = carritoService.agregarProducto(userId, productoId);
        return ResponseEntity.ok(carrito);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<?> eliminarItem(@PathVariable Long itemId) {
        carritoService.eliminarItem(itemId);
        return ResponseEntity.ok(Map.of("message", "Item eliminado"));

    }

    @DeleteMapping("/vaciar/{userId}")
    public ResponseEntity<?> vaciar(@PathVariable String userId) {
        carritoService.vaciarCarrito(userId);
        return ResponseEntity.ok(Map.of("message", "Carrito eliminado"));
    }

    @PostMapping("/comprar/{userId}")
    public ResponseEntity<?> realizarCompra(@PathVariable String userId) {
        try {
            HistorialCompra historial = carritoService.realizarCompra(userId);
            return ResponseEntity.ok(historial);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
