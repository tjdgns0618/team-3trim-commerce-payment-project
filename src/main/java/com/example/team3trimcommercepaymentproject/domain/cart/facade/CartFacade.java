package com.example.team3trimcommercepaymentproject.domain.cart.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.service.CartService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartFacade {

	private final CartService cartService;
	private final ProductService productService;

	@Transactional
	public CartItemAddResponse addItem(Long memberId, CartItemAddRequest request) {
		Cart cart = cartService.getOrCreateCart(memberId);
		// Member member = memberService.findMemberEntity(memberId);
		Product product = productService.findProductEntity(request.productId());
		CartItem cartItem = new CartItem(member, product, request.quantity());
		Long cartItemId = cartService.addItem(cartItem);
		return new CartItemAddResponse(cartItemId);
	}

}
