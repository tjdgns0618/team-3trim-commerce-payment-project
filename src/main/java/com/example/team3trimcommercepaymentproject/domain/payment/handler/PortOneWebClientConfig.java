package com.example.team3trimcommercepaymentproject.domain.payment.handler;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class PortOneWebClientConfig {

    @Bean
    public WebClient portOneWebClient() {
        return WebClient
                .builder()
                .baseUrl("https://api.portone.io")
                .build();
    }
}