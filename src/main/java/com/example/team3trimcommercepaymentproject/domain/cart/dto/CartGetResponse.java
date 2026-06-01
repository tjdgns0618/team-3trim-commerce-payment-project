package com.example.team3trimcommercepaymentproject.domain.cart.dto;

import java.util.List;

public record CartGetResponse(
	List<CartItemGetResponse> items,
	Integer totalAmount
) {}