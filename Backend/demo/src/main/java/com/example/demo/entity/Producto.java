package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="El título es obligatorio")
    private String titulo;

    @NotBlank(message="La descripción es obligatoria")
    private String descripcion;

    @NotBlank(message="La categoría es obligatoria")
    private String categoria;

    @NotNull(message="El precio es obligatorio")
    @Positive(message="El precio debe ser positivo")
    private Double precio;

    @NotBlank(message="El estado es obligatorio")
    private String estado;

    private String fotos;

    @NotNull(message="El propietario es obligatorio")
    private Long propietarioId;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFotos() { return fotos; }
    public void setFotos(String fotos) { this.fotos = fotos; }
    public Long getPropietarioId() { return propietarioId; }
    public void setPropietarioId(Long propietarioId) { this.propietarioId = propietarioId; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
}
