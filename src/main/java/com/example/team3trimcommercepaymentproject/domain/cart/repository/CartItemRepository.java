package com.example.team3trimcommercepaymentproject.domain.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	// 회원 고유 번호와 상품 고유 번호로 기존 장바구니 상품 조회 (중복 상품 담기 판별용)
	Optional<CartItem> findByMemberIdAndProductId(Long memberId, Long productId);

	Optional<CartItem> findByMemberIdAndId(Long memberId, Long id);

	Optional<List<CartItem>> findAllByMemberId(Long memberId);

	@Modifying // INSERT, UPDATE, DELETE 쿼리에 필수
	// 벌크 삭제 쿼리로 한번의 DELETE로 모든 장바구니 상품 삭제
	@Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId")
	void deleteAllByCartId(@Param("cartId") Long cartId);
}
