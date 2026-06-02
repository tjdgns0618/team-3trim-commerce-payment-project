package com.example.team3trimcommercepaymentproject.domain.payment.dto.reuquest;

public record PaymentConfirmRequest(
        Long orderId,
        String portonePaymentId
) {
}
