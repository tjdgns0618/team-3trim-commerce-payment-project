package com.example.team3trimcommercepaymentproject.domain.payment.dto.response;

public record PortOnePaymentResponse(
        String id,
        String status,
        Long amount
) {
    public boolean isPaid() {
        return "PAID".equals(status);
    }
}