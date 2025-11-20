package com.example.demo.DTO;

import lombok.Data;

@Data
public class ItemPagoDTO {
    private Long productoId;
    private String titulo;
    private Double precio;
    private Integer cantidad;
}
