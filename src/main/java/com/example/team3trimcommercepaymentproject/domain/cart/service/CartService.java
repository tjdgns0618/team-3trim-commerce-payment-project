package com.example.team3trimcommercepaymentproject.domain.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartGetResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemGetResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final CartRepository cartRepository;

	public Cart getOrCreateCart(Member member) {
		return cartRepository.findByMemberId(member.getId())
			.orElseGet(() -> cartRepository.save(new Cart(member)));
	}

	// 1. 사용자가 상품 담기를 누름
	// 2. 상품 정보 id, 사용자 id를 받음
	// 3. 이미 장바구니에 담긴 상품인지 검사 있다면 갯수 증가 끝
	// 4. 장바구니 아이템을 하나 만들어서 저장
	@Transactional
	public Long addItem(CartItem cartItem) {
		Optional<CartItem> existItem = cartItemRepository.findByMemberIdAndProductId(
			cartItem.getMember().getId(), cartItem.getProduct().getId());

		if (existItem.isPresent()) {
			CartItem foundItem = existItem.get();
			foundItem.addQuantity(cartItem.getQuantity());
			return foundItem.getId();
		} else {
			return cartItemRepository.save(cartItem).getId();
		}
	}

	@Transactional(readOnly = true)
	public CartGetResponse getAllCartItem(Long memberId) {
		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

		List<CartItemGetResponse> items = cart.getCartItems().stream()
			.map(CartItemGetResponse::from)
			.toList();

		int totalAmount = items.stream()
			.mapToInt(CartItemGetResponse::subtotal)
			.sum();

		return new CartGetResponse(items, totalAmount);
	}

}
