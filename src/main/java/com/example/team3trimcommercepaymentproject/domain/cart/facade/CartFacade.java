package com.example.team3trimcommercepaymentproject.domain.cart.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddRequest;
import com.example.team3trimcommercepaymentproject.domain.cart.dto.CartItemAddResponse;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.service.CartService;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartFacade {

	private final CartService cartService;
	private final ProductService productService;
	private final MemberService memberService;

	@Transactional
	public CartItemAddResponse addItem(Long memberId, CartItemAddRequest request) {
		Product product = productService.findProductEntity(request.productId());
		if (product.getSaleStatus() == Product.SaleStatus.SOLD_OUT) {
			throw new RuntimeException("상품 품절");
		}

		Member member = memberService.findMemberEntity(memberId);
		Cart cart = cartService.getOrCreateCart(member);

		CartItem cartItem = new CartItem(member, cart, product, request.quantity());
		Long cartItemId = cartService.addItem(cartItem);

		return new CartItemAddResponse(cartItemId);
	}

}
