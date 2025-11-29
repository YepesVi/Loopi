package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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
    private List<Producto> productos = new ArrayList<>();
}
