package com.example.demo.DTO;

import java.util.List;

import lombok.Data;

@Data
public class PagoCarritoDTO {
    public List<ItemPagoDTO> items;
}
