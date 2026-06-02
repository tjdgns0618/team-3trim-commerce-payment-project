package com.example.team3trimcommercepaymentproject.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	// Cart + CartItem + Product를 한 번에 페치해 N+1 방지
	@EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
	Optional<Cart> findByMemberId(Long memberId);
}
