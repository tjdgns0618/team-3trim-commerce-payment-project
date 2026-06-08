package com.example.team3trimcommercepaymentproject.domain.payment.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
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

    public static PaymentConfirmResponse from(Order order, Payment payment) {
        return new PaymentConfirmResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                payment.getStatus(),
                payment.getPgAmount(),
                payment.getUsedPoint(),
                payment.getEarnedPoint()
        );
    }

}