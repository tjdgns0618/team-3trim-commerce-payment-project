package com.example.team3trimcommercepaymentproject.domain.order.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Category;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long CART_ID = 1L;
    private static final Long CART_ITEM_ID = 7L;
    private static final Long PRODUCT_ID = 1L;

    private static final String PRODUCT_NAME = "무선 마우스";

    private static final int PRODUCT_PRICE = 29_000;
    private static final int STOCK_QUANTITY = 120;
    private static final int QUANTITY = 10;

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "전자기기";
    private static final Long ORDER_ID = 14L;
    private static final Long PAYMENT_ID = 14L;

    private static final Long USED_POINT = 0L;
    private static final Long TOTAL_AMOUNT = 290_000L;
    private static final Long PG_AMOUNT = 290_000L;
    private static final Long EARNED_POINT = 2_900L;
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Member createMember() {
        Member member = new Member("hong@example.com", "password", "홍길동", "01012345678");
        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
        return member;
    }

    private Category createCategory() {
        Category category = Category.builder()
                .name(CATEGORY_NAME)
                .build();

        ReflectionTestUtils.setField(category, "id", CATEGORY_ID);
        return category;
    }

    private Product createProduct() {
        Product product = Product.builder()
                .category(createCategory())
                .name(PRODUCT_NAME)
                .price(PRODUCT_PRICE)
                .stockQuantity(STOCK_QUANTITY)
                .description("테스트 상품")
                .saleStatus(Product.SaleStatus.ON_SALE)
                .build();

        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        return product;
    }

    private Cart createCart(Member member) {
        Cart cart = new Cart(member);
        ReflectionTestUtils.setField(cart, "id", CART_ID);
        return cart;
    }

    private CartItem createCartItem(Member member, Cart cart, Product product) {
        CartItem cartItem = new CartItem(member, cart, product, QUANTITY);
        ReflectionTestUtils.setField(cartItem, "id", CART_ITEM_ID);
        return cartItem;
    }

    @Test
    @DisplayName("주문/결제 동시 생성 성공 - 주문과 결제가 대기 상태로 함께 생성된다")
    void createOrderWithPayment_success() {
        // given
        Member member = createMember();
        Cart cart = createCart(member);
        Product product = createProduct();
        CartItem cartItem = createCartItem(member, cart, product);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(CART_ITEM_ID),
                USED_POINT
        );

        given(cartRepository.findByMemberId(MEMBER_ID))
                .willReturn(Optional.of(cart));

        given(cartItemRepository.findAllByMemberId(MEMBER_ID))
                .willReturn(List.of(cartItem));

        given(orderRepository.save(any(Order.class)))
                .willAnswer(invocation -> {
                    Order order = invocation.getArgument(0);

                    ReflectionTestUtils.setField(order, "id", ORDER_ID);
                    ReflectionTestUtils.setField(order.getPayment(), "id", PAYMENT_ID);

                    return order;
                });

        // when
        OrderCreateResponse response = orderService.createOrderWithPayment(MEMBER_ID, request);

        // then
        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.orderNumber()).startsWith("ORD-");
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);

        assertThat(response.payment()).isNotNull();
        assertThat(response.payment().paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(response.payment().portonePaymentId()).startsWith("PAY-ORD-");
        assertThat(response.payment().paymentStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(response.payment().totalAmount()).isEqualTo(TOTAL_AMOUNT);
        assertThat(response.payment().usedPoint()).isEqualTo(USED_POINT);
        assertThat(response.payment().pgAmount()).isEqualTo(PG_AMOUNT);
        assertThat(response.payment().earnedPoint()).isEqualTo(EARNED_POINT);
    }

    @Test
    @DisplayName("주문 생성 실패 - 장바구니 상품이 없으면 예외가 발생한다")
    void createOrderWithPayment_fail_cartEmpty() {
        // given
        Member member = createMember();
        Cart cart = createCart(member);

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(CART_ITEM_ID),
                USED_POINT
        );

        given(cartRepository.findByMemberId(MEMBER_ID))
                .willReturn(Optional.of(cart));

        given(cartItemRepository.findAllByMemberId(MEMBER_ID))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> orderService.createOrderWithPayment(MEMBER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.CART_EMPTY.getMessage());
    }
    @Test
    @DisplayName("주문 생성 실패 - 결제대기 주문이 이미 있으면 중복 주문 생성을 막는다")
    void createOrderWithPayment_fail_alreadyPendingOrder() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(CART_ITEM_ID),
                USED_POINT
        );

        given(orderRepository.existsByMemberIdAndStatus(MEMBER_ID, OrderStatus.PAYMENT_PENDING))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> orderService.createOrderWithPayment(MEMBER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_CANCELABLE.getMessage());
    }
}