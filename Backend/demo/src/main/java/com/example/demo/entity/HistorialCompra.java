package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
public class HistorialCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaCompra;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @OneToMany(mappedBy = "historial", cascade = CascadeType.ALL)
    //@JsonManagedReference
    private List<Producto> productos = new ArrayList<>();
}
