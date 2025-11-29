package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*; 

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@ToString(exclude = {"categoria", "propietario", "imagenes"}) 
@EqualsAndHashCode(exclude = {"id", "categoria", "propietario", "imagenes"})
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

    @OneToMany(
        mappedBy = "producto", 
        cascade = CascadeType.ALL, // Si se borra un Producto, se borran sus Imagen (en DB)
        orphanRemoval = true // Si quitas una Imagen del Set, se borra de la DB
    )
    @JsonManagedReference // Lado "padre" de la serialización JSON
    private Set<Imagen> imagenes = new HashSet<>(); 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false) // Mantiene la columna 'propietario_id'
    @JsonBackReference // Lado "hijo" de la serialización JSON
    private User propietario;
/* 
    @JsonProperty("propietarioId")
    public Long getPropietarioId() {
        if (propietario != null) {
            return propietario.getId();
        }
        return null;
    }*/

    @JsonProperty("fechaCreacion")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<CarritoItem> carritoItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_id")
    @JsonIgnore
    private HistorialCompra historial;


}