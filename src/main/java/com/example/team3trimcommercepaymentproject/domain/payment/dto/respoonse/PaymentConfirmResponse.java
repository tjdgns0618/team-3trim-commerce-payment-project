package com.example.team3trimcommercepaymentproject.domain.payment.dto.respoonse;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;

public record PaymentConfirmResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Long paidAmount,
        Long usedPoint,
        Long earnedPoint
) {
}