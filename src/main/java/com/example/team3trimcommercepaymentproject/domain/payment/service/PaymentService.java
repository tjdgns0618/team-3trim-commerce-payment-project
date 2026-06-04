package com.example.team3trimcommercepaymentproject.domain.payment.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.OrderCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.payment.repository.PaymentRepository;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import com.example.team3trimcommercepaymentproject.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PortOneClient portOneClient;

    @Value("${portone.webhook.secret-key}")
    private String webhookSecretKey;

    /**
     * 주문/결제 동시 생성
     **/
    @Transactional
    public OrderCreateResponse createPayment(Long memberId, OrderCreateRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findAllByMemberId(memberId);

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

    /**
     * 결제 확정
     **/
    @Transactional
    public PaymentConfirmResponse confirmfi(Long menderId, PaymentConfirmRequest confirmRequest) {

        Long orderId = confirmRequest.orderId();
        String portonePaymentId = confirmRequest.portonePaymentId();


        Order order = orderRepository.findOrderDetailByIdAndMemberId(orderId, menderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = order.getPayment();

        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!payment.getPortonePaymentId().equals(portonePaymentId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        order.complete();
        payment.complete();

        Cart cart = cartRepository.findByMemberId(menderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteAllByCartId(cart.getId());

        orderRepository.save(order);
        paymentRepository.save(payment);

        return finalizePayment(order, payment);
    }


    private PaymentConfirmResponse finalizePayment(Order order, Payment payment) {
        if (payment.isPaid()) {
            return toConfirmResponse(order, payment);
        }

        order.complete();
        payment.complete();

        return toConfirmResponse(order, payment);
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

    private PaymentConfirmResponse toConfirmResponse(Order order, Payment payment) {
        return new PaymentConfirmResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                payment.getStatus(),
                payment.getPgAmount(),
                payment.getUsedPoint(),
                payment.getEarnedPoint()
        );
    }
}


