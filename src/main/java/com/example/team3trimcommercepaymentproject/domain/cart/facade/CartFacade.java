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
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartFacade {

	private final CartService cartService;
	private final ProductService productService;
	private final MemberService memberService;

	// 다른 서비스를 이용하는 Cart의 로직을 구현하기 위해서 Facade에서 구현
	@Transactional
	public CartItemAddResponse addItem(Long memberId, CartItemAddRequest request) {
		Product product = productService.findProductEntity(request.productId());
		if (product.getSaleStatus() == Product.SaleStatus.SOLD_OUT) {
			throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
		}

		Member member = memberService.findMemberEntity(memberId);
		Cart cart = cartService.getOrCreateCart(member);

		CartItem cartItem = new CartItem(member, cart, product, request.quantity());
		Long cartItemId = cartService.addItem(cartItem);

		return new CartItemAddResponse(cartItemId);
	}

}
