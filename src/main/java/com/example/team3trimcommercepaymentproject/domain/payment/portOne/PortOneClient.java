package com.example.team3trimcommercepaymentproject.domain.payment.portOne;

import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final WebClient portOneWebClient;

    @Value("${portone.store-id}")
    private String storeId;

    public PortOnePaymentInfo getPayment(String portonePaymentId) {
        return portOneWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .queryParam("storeId", storeId)
                        .build(portonePaymentId)
                )
                .retrieve()
                .bodyToMono(PortOnePaymentInfo.class)
                .block();
    }
}