package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.entity.Carrito;
import com.example.demo.entity.CarritoItem;
import com.example.demo.entity.Producto;
import com.example.demo.entity.HistorialCompra;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.HistorialCompraRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class HistorialCompraService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private HistorialCompraRepository historialCompraRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public HistorialCompra generarHistorial(String userId) {

        User user = userRepository.findByCedula(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoRepository.findByUser_Cedula(userId)
                .orElseThrow(() -> new RuntimeException("El usuario no tiene carrito"));

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        HistorialCompra historial = new HistorialCompra();
        historial.setFechaCompra(LocalDateTime.now());
        historial.setUsuario(user);

        historial = historialCompraRepository.save(historial);

        for (CarritoItem item : carrito.getItems()) {

            Producto producto = item.getProducto();

            producto.setHistorial(historial);
            producto.setEstado("VENDIDO");

            productoRepository.save(producto);

            historial.getProductos().add(producto);

            carritoItemRepository.deleteByProducto_Id(producto.getId());
        }

        historial = historialCompraRepository.save(historial);

        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return historial;
    }

    public List<HistorialCompra> obtenerHistorial(String cedula) {
        return historialCompraRepository.findByUsuario_CedulaOrderByFechaCompraDesc(cedula);
    }

}
