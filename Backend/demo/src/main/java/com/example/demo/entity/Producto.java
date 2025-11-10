package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*; 

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@ToString(exclude = {"categoria", "propietario"}) 
@EqualsAndHashCode(exclude = {"id", "categoria", "propietario"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="El título es obligatorio")
    private String titulo;

    @NotBlank(message="La descripción es obligatoria")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false) // 'nullable = false' si un producto SIEMPRE debe tener categoría
    private Categoria categoria;

    @NotNull(message="El precio es obligatorio")
    @Positive(message="El precio debe ser positivo")
    private Double precio;

    @NotBlank(message="El estado es obligatorio")
    private String estado;

    private String fotos; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false) // Mantiene la columna 'propietario_id'
    @JsonBackReference // Lado "hijo" de la serialización JSON
    private User propietario;

    @JsonProperty("fechaCreacion")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

}