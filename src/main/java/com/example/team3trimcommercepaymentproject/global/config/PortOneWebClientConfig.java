package com.example.team3trimcommercepaymentproject.global.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PortOneWebClientConfig {

    @Bean
    public WebClient portOneWebClient(
            @Value("${portone.api-base-url}") String baseUrl,
            @Value("${portone.api-secret}") String apiSecret
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "PortOne " + apiSecret)
                .build();
    }
}