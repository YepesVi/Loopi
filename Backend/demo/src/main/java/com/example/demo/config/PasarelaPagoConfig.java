package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

@Configuration
public class PasarelaPagoConfig {

    public PasarelaPagoConfig() {
        MercadoPagoConfig.setAccessToken("APP_USR-8266653278548866-112012-4f4aa6ec4f1d029c3f1a6841444a0aa3-3005053160");
    }
}
