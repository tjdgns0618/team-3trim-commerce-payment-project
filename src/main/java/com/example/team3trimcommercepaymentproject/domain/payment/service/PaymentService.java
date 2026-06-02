package com.example.team3trimcommercepaymentproject.domain.payment.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.repository.MemberRepository;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.orderItem.repository.OrderItemRepository;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final JwtProvider jwtProvider;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 주문/결제 동시 생성
     **/
    @Transactional
    public OrderCreateResponse createPayment(Long memberId, OrderCreateRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findAllByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        List<Long> cartItemIds = request.cartItemIds();

        List<CartItem> targetCartItems;

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            targetCartItems = cartItems;
        } else {
            targetCartItems = cartItems.stream()
                    .filter(cartItem -> cartItemIds.contains(cartItem.getId()))
                    .toList();

            if (targetCartItems.isEmpty() || targetCartItems.size() != cartItemIds.size()) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
        }

        Long totalAmount = 0L;

        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            Long price = product.getPrice().longValue();
            Integer quantity = cartItem.getQuantity();
            totalAmount += price * quantity;
        }

        Long usedPoint = request.usedPoint() == null ? 0L : request.usedPoint();

        if (usedPoint > totalAmount) {
            throw new BusinessException(ErrorCode.POINT_EXCEEDS_ORDER_AMOUNT);
        }

        Long pgAmount = totalAmount - usedPoint;
        Long earnedPoint = pgAmount / 100;

        String orderNumber = generateOrderNumber();
        String portonePaymentId = generatePortonePaymentId(orderNumber);

        Order order = Order.builder()
                .member(cart.getMember())
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .usedPoint(usedPoint)
                .pgAmount(pgAmount)
                .earnedPoint(earnedPoint)
                .build();

        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productNameSnapshot(product.getName())
                    .priceSnapshot(product.getPrice().longValue())
                    .quantity(cartItem.getQuantity())
                    .build();

            order.addOrderItem(orderItem);

            product.decreaseStock(cartItem.getQuantity());
        }

        Payment payment = Payment.builder()
                .portonePaymentId(portonePaymentId)
                .totalAmount(totalAmount)
                .usedPoint(usedPoint)
                .pgAmount(pgAmount)
                .earnedPoint(earnedPoint)
                .build();

        order.assignPayment(payment);

        Order savedOrder = orderRepository.save(order);
        Payment savedPayment = savedOrder.getPayment();

        return new OrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getStatus(),
                new PaymentCreateResponse(
                        savedPayment.getId(),
                        savedPayment.getPortonePaymentId(),
                        savedPayment.getStatus(),
                        savedPayment.getTotalAmount(),
                        savedPayment.getUsedPoint(),
                        savedPayment.getPgAmount(),
                        savedPayment.getEarnedPoint()
                )
        );
    }

    // 나중에 삭제 될 예정
    private String generatePortonePaymentId(String orderNumber) {
        String random = java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        return "PAY-" + orderNumber + "-" + random;
    }

    private String generateOrderNumber() {
        String date = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String random = java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        return "ORD-" + date + "-" + random;
    }
}
