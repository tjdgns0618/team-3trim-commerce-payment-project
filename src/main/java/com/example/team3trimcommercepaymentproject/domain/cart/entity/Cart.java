package com.example.team3trimcommercepaymentproject.domain.cart.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.team3trimcommercepaymentproject.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 한 사용자마다 한 장바구니만 가지고 있기 때문에 1대1 연관관계 매핑
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 양방향 매핑 장바구니 삭제 시 안에 든 상품도 일괄 삭제되도록 cascade 설정
	// 만약에 상품이 엄청나게 많아질 경우 성능 문제가 발생할 수 있다.
	// 벌크 삭제 쿼리 (JPQL을 통해서 한번에 삭제하는게 더 효과적일 것 같다.) 고려
	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();
}
