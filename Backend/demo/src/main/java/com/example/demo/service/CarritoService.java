package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.CarritoItem;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.repository.CarritoItemRepository;
import com.example.demo.repository.CarritoRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

@Service
public class CarritoService {
    
    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public Carrito getCarritoDeUsuario(String userId) {
        return carritoRepository.findByUser_Cedula(userId)
            .orElseGet(() -> crearCarritoParaUsuario(userId));
    }

    @Transactional
    public Carrito crearCarritoParaUsuario(String userId) {
        User user = userRepository.findByCedula(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        Optional<Carrito> carritoExistente = carritoRepository.findByUser_Cedula(userId);

        if (carritoExistente.isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene un carrito activo.");
        }

        Carrito carrito = new Carrito();
        carrito.setUser(user);

        return carritoRepository.save(carrito);
    }


    @Transactional
    public Carrito agregarProducto(String userId, Long productoId) {

        Carrito carrito = getCarritoDeUsuario(userId);

        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no existe"));

        if (producto.getEstado().equalsIgnoreCase("Disponible")) {
            throw new IllegalArgumentException("No hay disponibilidad de este producto.");
        }

        boolean yaEstaEnCarrito = carrito.getItems().stream()
            .anyMatch(i -> i.getProducto().getId().equals(productoId));

        if (yaEstaEnCarrito) {
            throw new IllegalArgumentException("Este producto ya está agregado al carrito.");
        }

        CarritoItem item = new CarritoItem();
        item.setCarrito(carrito);
        item.setProducto(producto);

        carrito.getItems().add(item);

        carritoItemRepository.save(item);

        return carritoRepository.save(carrito);
    }


    @Transactional
    public void eliminarItem(Long itemId) {
        carritoItemRepository.deleteById(itemId);
    }

    @Transactional
    public void vaciarCarrito(String userId) {
        Carrito carrito = getCarritoDeUsuario(userId);
        carritoItemRepository.deleteAll(carrito.getItems());
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    // ------------------------------------------------------
    // 6) CHECKOUT BÁSICO (solo para futura integración)
    // ------------------------------------------------------
    /*@Transactional
    public void checkout(String userId) {
        Carrito carrito = getCarritoDeUsuario(userId);

        for (CarritoItem item : carrito.getItems()) {
            if (!item.getProducto().getEstado().equalsIgnoreCase("Disponible")) {
                throw new IllegalArgumentException("Stock insuficiente en " + item.getProducto().getTitulo());
            }
        }

        for (CarritoItem item : carrito.getItems()) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        // Vaciar carrito
        vaciarCarrito(userId);
    }*/
}
