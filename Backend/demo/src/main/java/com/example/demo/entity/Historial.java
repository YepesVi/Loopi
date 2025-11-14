package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "historial")



public class Historial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el producto
   @ManyToOne
   @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "producto_id", nullable = true)
    private Producto producto;




    private String estado;
    private String accion;
    private String usuario;
    private Long propietarioId;

    public Long getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(Long propietarioId) {
        this.propietarioId = propietarioId;
    }

    private LocalDateTime fechaRegistro;
    

    // ✅ Constructor vacío (obligatorio para JPA)
    public Historial() {}

    // ✅ Constructor personalizado (el que te falta)
    public Historial(Producto producto, String estado, String accion, String usuario) {
        this.producto = producto;
        this.estado = estado;
        this.accion = accion;
        this.usuario = usuario;
        this.fechaRegistro = LocalDateTime.now();
        
    }

    // ✅ Getters y setters
    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
