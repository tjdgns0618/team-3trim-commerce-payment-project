package com.example.team3trimcommercepaymentproject.domain.payment.portOne;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PortOneCancelRequest;

@Component
@RequiredArgsConstructor
@Slf4j
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

    // portOne으로 결제 취소 post요청
    @Transactional
    public void cancelPayment(String paymentId, String reason) {
        String idempotencyKey = UUID.randomUUID().toString();
        log.info("PortOne 결제 취소 요청: paymentId={}, reason={}, idempotencyKey={}", paymentId, reason, idempotencyKey);

        int maxRetries = 3;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                portOneWebClient.post()
                    .uri("/payments/{paymentId}/cancel", paymentId)
                    .header("Idempotency-Key", idempotencyKey) // 같은 키 유지
                    .bodyValue(new PortOneCancelRequest(reason, storeId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("PortOne 취소 실패 응답: {}", body);
                                return Mono.error(new RuntimeException("PortOne 취소 실패: " + body));
                            }))
                    .toBodilessEntity()
                    .block(); // 동기식으로 결과를 기다림
                return;  // 성공하면 종료
            } catch (WebClientRequestException e) {
                // 네트워크 타임아웃 및 연결 오류 -> 재시도
                log.warn("PortOne 취소 요청 타임아웃/연결오류 (시도 {}/{})", attempt, maxRetries);
                if (attempt == maxRetries)
                    throw e;
            }
        }
    }
}