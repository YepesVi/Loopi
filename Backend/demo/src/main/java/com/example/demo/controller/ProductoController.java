package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repo;

    public ProductoController(ProductoRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/publicados")
    public Page<Producto> listarProductosPublicos(Pageable pageable) {
        return repo.findByEstadoIgnoreCase("PUBLICADO", pageable);
    }

    @GetMapping("/mis-publicaciones")
    public ResponseEntity<Page<Producto>> listarPublicacionesPorPropietario(
            @RequestParam Long propietarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> publicaciones = repo.findByPropietarioId(propietarioId, pageable);
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Producto> listarProductosPorUsuario(@PathVariable Long usuarioId) {
        return repo.findByPropietarioId(usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Optional<Producto> producto = repo.findById(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crearOModificarProducto(@Valid @RequestBody Producto producto, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errores = result.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errores);
        }
        Producto guardado = repo.save(producto);
        return ResponseEntity.ok(guardado);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String folder = "uploads/";
            Path folderPath = Paths.get(folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path filePath = folderPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            return ResponseEntity.ok(filePath.toString());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir archivo");
        }
    }

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
            String folder = "uploads/";
            Path folderPath = Paths.get(folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path filePath = folderPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            Producto producto = new Producto();
            producto.setTitulo(titulo);
            producto.setDescripcion(descripcion);
            producto.setCategoria(categoria);
            producto.setPrecio(precio);
            producto.setEstado(estado);
            producto.setPropietarioId(propietarioId);
            producto.setFotos("/uploads/" + filename);

            Producto nuevo = repo.save(producto);
            return ResponseEntity.ok(nuevo);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id,
            @Valid @RequestBody Producto producto, BindingResult result, @RequestParam Long usuarioId) {
        if (result.hasErrors()) {
            List<String> errores = result.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errores);
        }

        return repo.findById(id).map(orig -> {
            if (!orig.getPropietarioId().equals(usuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            producto.setId(id);
            Producto actualizado = repo.save(producto);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id, @RequestParam Long usuarioId) {
        return repo.findById(id).map(orig -> {
            if (!orig.getPropietarioId().equals(usuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            repo.deleteById(id);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Producto>> buscarProductos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> resultados = repo.buscarConFiltros(titulo, categoria, precioMin, precioMax, pageable);
        return ResponseEntity.ok(resultados);
    }
}