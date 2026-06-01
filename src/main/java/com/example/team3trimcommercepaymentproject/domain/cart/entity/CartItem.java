package com.example.team3trimcommercepaymentproject.domain.cart.entity;

import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
// uniqueConstraints : 복합 UNIQUE 제약
// 이유 : 같은 사용자가 같은 상품을 장바구니에 중복으로 담아도 cart_id, product_id 조합당 행이 1개만 존재하도록 DB 레벨에서 원천 차단
// 그냥 속성에서 Column에 unique 설정을 하면 안되는 이유
// cart_id의 경우 장바구니 1번에는 상품을 딱 1개만 담을 수 있게 된다. -> 상품 2개를 담으려 하면 에러
// product_id의 경우 상품이 전 세계 사용자 중 단 1명만이 장바구니에 담을 수 있게 된다. -> 다른 사람이 같은 상품을 담으려 하면 에러
@Table(name = "cart_items", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
public class CartItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	// 장바구니에 담은 수량을 한개 늘리는 메서드
	public void increaseQuantity() {
		int increasedQuantity = this.quantity + 1;

		if(increasedQuantity > product.getStockQuantity()) {
			throw new RuntimeException("장바구니 상품 갯수 1개 추가 요청, 재고 초과");
		}

		this.quantity++;
	}

	// 장바구니에 담은 수량을 한개 줄이는 메서드
	public void decreaseQuantity() {
		int decreasedQuantity = this.quantity - 1;

		if (decreasedQuantity < 1) {
			throw new RuntimeException("장바구니 상품 갯수 1개 감소 요청, 1개 미만 담기 불가능");
		}

		this.quantity--;
	}

	// 장바구니에 담은 수량을 quantity만큼 추가하는 메서드
	public void addQuantity(Integer quantity) {
		int totalQuantity = this.quantity + quantity;

		if (totalQuantity > product.getStockQuantity()) {
			throw new RuntimeException("장바구니 담기 요청, 재고 초과");
		}

		this.quantity = totalQuantity;
	}

	// 장바구니에 담은 수량을 수정하는 메서드
	public void updateQuantity(Integer quantity) {
		if (quantity > product.getStockQuantity()) {
			throw new IllegalArgumentException("장바구니 재고 수량 변경 요청, 재고 초과");
		}

		this.quantity = quantity;
	}
}
