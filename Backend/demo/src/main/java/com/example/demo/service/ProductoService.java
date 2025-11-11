package com.example.demo.service;

import com.example.demo.entity.Categoria;
import com.example.demo.entity.Imagen;
import com.example.demo.entity.Producto;
import com.example.demo.entity.User;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor 
public class ProductoService {

    // Inyección de dependencias (final)
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService; // Para la lógica de descendientes
    private final UserRepository userRepository;
    private final CloudinaryUploadService cloudinaryUploadService;
    
    // --- Métodos CRUD ---

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con ID: " + id));
    }

    @Transactional
    public Producto crearProducto(Producto producto, Long categoriaId,Long propietarioId, List<MultipartFile> files) throws IOException {
        
        Categoria categoria = categoriaService.findById(categoriaId)
                .orElseThrow(() -> new NoSuchElementException("Categoría no encontrada con ID: " + categoriaId));
        producto.setCategoria(categoria);

        User propietario = userRepository.findById(propietarioId) //
                .orElseThrow(() -> new NoSuchElementException("Usuario (Propietario) no encontrado con ID: " + propietarioId));
        producto.setPropietario(propietario);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    // 1. Subir a la carpeta "productos"
                    Map<String, String> uploadResult = cloudinaryUploadService.uploadFile(file, "productos");
                    
                    // 2. Crear la entidad Imagen
                    Imagen imagen = Imagen.builder()
                            .secureUrl(uploadResult.get("secure_url"))
                            .publicId(uploadResult.get("public_id"))
                            .producto(producto) // Relación inversa
                            .build();
                    
                    // 3. Añadir al Set (Gracias a Cascade.ALL, se guarda junto al producto)
                    producto.getImagenes().add(imagen);
                }
            }
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(Long id, Producto details, Long categoriaId, Long propietarioId, List<MultipartFile> files) throws IOException {
        Producto producto = obtenerPorId(id); // Reutiliza el método

        // Actualizar campos
        producto.setTitulo(details.getTitulo());
        producto.setDescripcion(details.getDescripcion());
        producto.setPrecio(details.getPrecio());
        producto.setEstado(details.getEstado());

        // Asignar nueva categoría
        Categoria categoria = categoriaService.findById(categoriaId)
                .orElseThrow(() -> new NoSuchElementException("Categoría no encontrada con ID: " + categoriaId));
        producto.setCategoria(categoria);

        User propietario = userRepository.findById(propietarioId) //
                .orElseThrow(() -> new NoSuchElementException("Usuario (Propietario) no encontrado con ID: " + propietarioId));
        producto.setPropietario(propietario);

        // Si se enviaron nuevos archivos, reemplazar los antiguos
        if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
            
            // 1. Borrar imágenes antiguas de Cloudinary
            for (Imagen imgExistente : producto.getImagenes()) {
                cloudinaryUploadService.deleteFile(imgExistente.getPublicId());
            }
            
            // 2. Limpiar la colección (orphanRemoval=true borra de la DB)
            producto.getImagenes().clear();

            // 3. Subir nuevas imágenes (lógica idéntica a crearProducto)
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    Map<String, String> uploadResult = cloudinaryUploadService.uploadFile(file, "productos");
                    Imagen imagen = Imagen.builder()
                            .secureUrl(uploadResult.get("secure_url"))
                            .publicId(uploadResult.get("public_id"))
                            .producto(producto)
                            .build();
                    producto.getImagenes().add(imagen);
                }
            }
        }
        
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) throws IOException {
        if (!productoRepository.existsById(id)) {
            throw new NoSuchElementException("Producto no encontrado con ID: " + id);
        }
        Producto producto = obtenerPorId(id); // 1. Obtener el producto
        
        // 2. Iterar y borrar cada imagen de Cloudinary
        for (Imagen img : producto.getImagenes()) {
            cloudinaryUploadService.deleteFile(img.getPublicId());
        }
        // (Opcional: lógica para borrar archivos del disco)
        productoRepository.deleteById(id);
    }

    // --- Lógica de Búsqueda (Movida del Controller) ---

    @Transactional(readOnly = true)
    public List<Producto> historialPublicaciones(Long propietarioId, String estado) {
        List<Producto> productos = productoRepository.findByPropietarioId(propietarioId);
        if (estado != null && !estado.isEmpty()) {
            return productos.stream()
                    .filter(p -> p.getEstado().equalsIgnoreCase(estado))
                    .collect(Collectors.toList());
        }
        return productos;
    }

    @Transactional(readOnly = true)
    public List<Producto> obtenerPublicados() {
        return productoRepository.findByEstadoIgnoreCase("publicado");
    }

    @Transactional
    public Producto actualizarEstadoProducto(Long id, String nuevoEstado) {
        Producto producto = obtenerPorId(id);
        producto.setEstado(nuevoEstado);
        return productoRepository.save(producto);
    }

    // --- BÚSQUEDA AVANZADA (CON LÓGICA DE DESCENDIENTES) ---

    @Transactional(readOnly = true)
    public Page<Producto> buscarConFiltros(String titulo, Long categoriaId, Double precioMin, Double precioMax, String estado, Long propietarioId, Pageable pageable) {
        List<Long> idsCategoriasBusqueda = null;

       if (categoriaId != null) {
            // Llama al nuevo método que ya devuelve la lista de IDs (padre + hijos)
            idsCategoriasBusqueda = categoriaService.findAllDescendantIdsWithSelf(categoriaId);
        }
        String tituloLike = (titulo != null && !titulo.isEmpty()) ? "%" + titulo + "%" : null;
        String estadoLike = (estado != null && estado.trim().isEmpty() == false) ? "%" + estado + "%" : null;
        
        int categoriaCount = (idsCategoriasBusqueda != null) ? idsCategoriasBusqueda.size() : 0;
        
        List<Long> categoriasParam = (categoriaCount > 0) ? idsCategoriasBusqueda : null;

        // Llamar al repositorio actualizado
        return productoRepository.buscarConFiltros(tituloLike, categoriasParam, categoriaCount, precioMin, precioMax, estadoLike, propietarioId, pageable);
    }

    
}