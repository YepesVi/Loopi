package com.example.demo.DTO;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemPagoDTO {
    private Long productoId;
    private String titulo;
    private BigDecimal precio;
}
