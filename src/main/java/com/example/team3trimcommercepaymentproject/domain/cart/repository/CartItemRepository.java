package com.example.team3trimcommercepaymentproject.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	// 회원 고유 번호와 상품 고유 번호로 기존 장바구니 상품 조회 (중복 상품 담기 판별용)
	Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);


}
