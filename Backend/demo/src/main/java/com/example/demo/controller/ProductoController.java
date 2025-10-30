package com.example.demo.controller;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository repo;

    private final String UPLOAD_DIR = "uploads/";

    // ✅ Listar todos los productos
    @GetMapping
    public List<Producto> listarTodos() {
        return repo.findAll();
    }

    // ✅ Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Crear producto con imagen
    @PostMapping("/crear-con-imagen")
    public ResponseEntity<Producto> crearProductoConImagen(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoria") String categoria,
            @RequestParam("precio") Double precio,
            @RequestParam("estado") String estado,
            @RequestParam("propietarioId") Long propietarioId,
            @RequestParam("file") MultipartFile file) {

        try {
            Path folder = Paths.get(UPLOAD_DIR);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path filePath = folder.resolve(filename);
            Files.write(filePath, file.getBytes());

            Producto producto = new Producto();
            producto.setTitulo(titulo);
            producto.setDescripcion(descripcion);
            producto.setCategoria(categoria);
            producto.setPrecio(precio);
            producto.setEstado(estado);
            producto.setPropietarioId(propietarioId);
            producto.setFotos("/uploads/" + filename);

            return ResponseEntity.ok(repo.save(producto));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Actualizar producto (con opción de cambiar imagen)
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoria") String categoria,
            @RequestParam("precio") Double precio,
            @RequestParam("estado") String estado,
            @RequestParam("propietarioId") Long propietarioId,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Optional<Producto> productoExistente = repo.findById(id);
        
        if (productoExistente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Producto producto = productoExistente.get();
        producto.setTitulo(titulo);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setEstado(estado);
        producto.setPropietarioId(propietarioId);

        // Si se envía una nueva imagen, la guardamos y actualizamos
        if (file != null && !file.isEmpty()) {
            try {
                Path folder = Paths.get(UPLOAD_DIR);
                if (!Files.exists(folder)) {
                    Files.createDirectories(folder);
                }

                String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
                Path filePath = folder.resolve(filename);
                Files.write(filePath, file.getBytes());

                producto.setFotos("/uploads/" + filename);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok(repo.save(producto));
    }

    // ✅ Eliminar producto
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    @GetMapping("/usuario/{propietarioId}/historial")
public ResponseEntity<List<Producto>> historialPublicaciones(
        @PathVariable Long propietarioId,
        @RequestParam(required = false) String estado) {

    List<Producto> productos;

    if (estado != null && !estado.isEmpty()) {
        productos = repo.findByPropietarioId(propietarioId)
                        .stream()
                        .filter(p -> p.getEstado().equalsIgnoreCase(estado))
                        .toList();
    } else {
        productos = repo.findByPropietarioId(propietarioId);
    }

    if (productos.isEmpty()) {
        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(productos);
}

}
=======
}
>>>>>>> 8e2d5e8b2b651e54bdc3d4c1a00701920a69bea3
