package com.example.team3trimcommercepaymentproject.domain.payment.portOne;

public record PortOnePaymentInfo(
        String paymentId,
        String status,
        Long paidAmount
) {
    public boolean isPaid() {
        return "PAID".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status)
                || "CANCELLED".equals(status)
                || "CANCELED".equals(status);
    }

}