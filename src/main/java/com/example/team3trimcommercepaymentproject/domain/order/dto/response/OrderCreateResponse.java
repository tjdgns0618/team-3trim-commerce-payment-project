package com.example.team3trimcommercepaymentproject.domain.order.dto.response;

import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.respoonse.PaymentCreateResponse;

public record OrderCreateResponse(
        Long orderId,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentCreateResponse payment
) {
}
