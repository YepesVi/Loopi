package com.example.demo.controller;

import com.example.demo.entity.Producto;
import com.example.demo.service.ProductoService; // 👈 IMPORTAR SERVICIO

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor 
public class ProductoController {

    private final ProductoService productoService; 


    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/publicados")
    public ResponseEntity<List<Producto>> obtenerPublicados() {
        List<Producto> productos = productoService.obtenerPublicados();
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productos);
    }


    @GetMapping("/buscar")
    public ResponseEntity<Page<Producto>> buscarProductos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId, 
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long propietarioId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<Producto> productos = productoService.buscarConFiltros(titulo, categoriaId,
                                    precioMin, precioMax, estado, propietarioId, pageable);
        return ResponseEntity.ok(productos);
    }


    @GetMapping("/usuario/{propietarioId}/historial")
    public ResponseEntity<List<Producto>> historialPublicaciones(
            @PathVariable Long propietarioId,
            @RequestParam(required = false) String estado) {
        List<Producto> productos = productoService.historialPublicaciones(propietarioId, estado);
        if (productos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(productos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.obtenerPorId(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/crear-con-imagen")
    public ResponseEntity<?> crearProductoConImagen(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoriaId") Long categoriaId, 
            @RequestParam("precio") Double precio,
            @RequestParam("estado") String estado,
            @RequestParam("propietarioId") Long propietarioId,
            @RequestParam("file") List<MultipartFile> files) {
        
        try {
            Producto producto = new Producto();
            producto.setTitulo(titulo);
            producto.setDescripcion(descripcion);
            producto.setPrecio(precio);
            producto.setEstado(estado);
            
            Producto nuevoProducto = productoService.crearProducto(producto, categoriaId, propietarioId, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar imágenes: " + e.getMessage());
        }
    }


    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoriaId") Long categoriaId, 
            @RequestParam("precio") Double precio,
            @RequestParam("estado") String estado,
            @RequestParam("propietarioId") Long propietarioId,
            @RequestParam(value = "file", required = false) List<MultipartFile> files) { // 👈 CAMBIO: Acepta Lista

        try {
            Producto details = new Producto();
            details.setTitulo(titulo);
            details.setDescripcion(descripcion);
            details.setPrecio(precio);
            details.setEstado(estado);

            Producto productoActualizado = productoService.actualizarProducto(id, details, categoriaId, propietarioId, files);
            return ResponseEntity.ok(productoActualizado);
            
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar imágenes: " + e.getMessage());
        }
    }


    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) throws IOException {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoProducto(
            @PathVariable Long id,
            @RequestBody String nuevoEstado) {
        try {
            String estadoLimpio = nuevoEstado.replaceAll("\"", ""); 
            Producto producto = productoService.actualizarEstadoProducto(id, estadoLimpio);
            return ResponseEntity.ok(producto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

}