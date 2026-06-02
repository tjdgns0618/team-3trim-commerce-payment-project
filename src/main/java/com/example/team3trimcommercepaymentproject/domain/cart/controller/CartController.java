package com.example.team3trimcommercepaymentproject.domain.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartGetResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartUpdateRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.facade.CartFacade;
import com.example.team3trimcommercepaymentproject.domain.cart.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/cart/items")
public class CartController {

	private final CartService cartService;
	private final CartFacade cartFacade;

	@PostMapping
	public ResponseEntity<CartItemAddResponse> addItem(
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody CartItemAddRequest request
	) {
		CartItemAddResponse response = cartFacade.addItem(memberId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<CartGetResponse> getAllCartItem(@AuthenticationPrincipal Long memberId) {
		CartGetResponse response = cartService.getAllCartItem(memberId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PatchMapping("/{itemId}")
	public ResponseEntity<CartItemResponse> updateItem(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long itemId,
		@Valid @RequestBody CartUpdateRequest request
	) {
		CartItemResponse response = cartService.updateQuantity(memberId, itemId, request);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
