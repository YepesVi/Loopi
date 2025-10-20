package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="El título es obligatorio")
    @Size(max=100, message="El título máximo es de 100 caracteres")
    private String titulo;

    @NotBlank(message="La descripción es obligatoria")
    @Size(max=500, message="La descripción máximo es de 500 caracteres")
    private String descripcion;


    @NotBlank(message="La categoría es obligatoria")
    private String categoria;

    @NotNull(message="El precio es obligatorio")
    @Positive(message="El precio debe ser positivo")
    private Double precio;

    @NotBlank(message="El estado es obligatorio")
    private String estado;

    // Se asume que fotos es una cadena de rutas separadas por comas
    @Size(max=200, message="La ruta de fotos debe ser menor a 200 caracteres")
    private String fotos;

    @NotNull(message="El propietario es obligatorio")
    private Long propietarioId;

    private LocalDateTime fechaPublicacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

    public Long getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(Long propietarioId) {
        this.propietarioId = propietarioId;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }


    
}
