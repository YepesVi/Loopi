package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sendgrid.SendGrid;

@Configuration
public class SendGridConfig {

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(System.getenv("SENDGRID_API_KEY"));
    }
}
