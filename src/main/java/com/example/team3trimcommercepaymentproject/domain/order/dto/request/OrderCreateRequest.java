package com.example.team3trimcommercepaymentproject.domain.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;

public record OrderCreateRequest(
        List<Long> cartItemIds,
		@Min(0)
        Long usedPoint
) {
}
