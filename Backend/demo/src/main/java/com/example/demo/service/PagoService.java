package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.DTO.PagoCarritoDTO;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
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
            System.out.println("Items: " + carritoDTO.getItems());

            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
            PreferenceClient client = new PreferenceClient();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/pago-exitoso")
                    .failure("http://localhost:4200/pago-fallo")
                    .pending("http://localhost:4200/pago-pendiente")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(
                            carritoDTO.getItems().stream().map(item ->
                                    PreferenceItemRequest.builder()
                                            .title(item.getTitulo())
                                            .unitPrice(item.getPrecio())
                                            .currencyId("COP")
                                            .quantity(1)
                                            .build()
                            ).toList()
                    )
                    .backUrls(backUrls)
                    .build();

            Preference pref = client.create(request);

            System.out.println("Preference creada:");
            System.out.println("ID: " + pref.getId());
            System.out.println("Init Point: " + pref.getInitPoint());
            System.out.println("Init Point: " + pref.getSandboxInitPoint());

            return pref;

        } catch (MPApiException e) {
            System.out.println("=== API ERROR MP ===");
            System.out.println("Status: " + e.getApiResponse().getStatusCode());
            System.out.println("Content: " + e.getApiResponse().getContent());
            System.out.println("====================");

            throw new RuntimeException("Error creando preferencia de pago", e);
        } catch (MPException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creando preferencia de pago", e);
        }
    }
}

