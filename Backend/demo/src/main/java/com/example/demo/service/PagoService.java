package com.example.demo.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.demo.DTO.PagoCarritoDTO;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import org.springframework.beans.factory.annotation.Value;

@Service
public class PagoService {

    private final String mercadoPagoAccessToken;

    public PagoService(@Value("${mercadopago.access-token}") String mercadoPagoAccessToken) {
        this.mercadoPagoAccessToken = mercadoPagoAccessToken;
    }

    public Preference crearPreferencia(PagoCarritoDTO carritoDTO) {

    try {

        MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

        PreferenceClient client = new PreferenceClient();

        PreferenceRequest request = PreferenceRequest.builder()
                .items(
                        carritoDTO.getItems().stream().map(item ->
                                PreferenceItemRequest.builder()
                                        .title(item.getTitulo())
                                        .quantity(item.getCantidad())
                                        .unitPrice(BigDecimal.valueOf(item.getPrecio()))
                                        .currencyId("COP")
                                        .build()
                        ).toList()
                )
                .build();

        return client.create(request);

    } catch (MPException | MPApiException e) {
        throw new RuntimeException("Error creando preferencia de pago", e);
    }
}

}

