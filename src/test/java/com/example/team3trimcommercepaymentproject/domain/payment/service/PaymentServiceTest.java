package com.example.team3trimcommercepaymentproject.domain.payment.service;


import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.entity.OrderStatus;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
import com.example.team3trimcommercepaymentproject.domain.payment.repository.PaymentRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {


    private static final Long MEMBER_ID = 1L;
    private static final Long ORDER_ID = 14L;
    private static final Long PAYMENT_ID = 14L;

    private static final String ORDER_NUMBER = "ORD-20260604-522DFF53";
    private static final String PORTONE_PAYMENT_ID = "PAY-ORD-20260604-522DFF53-64DD5C19";

    private static final Long TOTAL_AMOUNT = 379_000L;
    private static final Long USED_POINT = 0L;
    private static final Long PG_AMOUNT = 379_000L;
    private static final Long EARNED_POINT = 3_790L;

    private static final String MEMBER_EMAIL = "hong@example.com";
    private static final String MEMBER_PASSWORD = "encryptedPassword";
    private static final String MEMBER_NAME = "홍길동";
    private static final String MEMBER_PHONE_NUMBER = "01012345678";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order createPendingOrder(Member member) {
        Order order = Order.builder()
                .member(member)
                .orderNumber(ORDER_NUMBER)
                .totalAmount(TOTAL_AMOUNT)
                .usedPoint(USED_POINT)
                .pgAmount(PG_AMOUNT)
                .earnedPoint(EARNED_POINT)
                .build();

        ReflectionTestUtils.setField(order, "id", ORDER_ID);
        return order;
    }

    private Payment createReadyPayment() {
        Payment payment = Payment.builder()
                .portonePaymentId(PORTONE_PAYMENT_ID)
                .totalAmount(TOTAL_AMOUNT)
                .usedPoint(USED_POINT)
                .pgAmount(PG_AMOUNT)
                .earnedPoint(EARNED_POINT)
                .build();

        ReflectionTestUtils.setField(payment, "id", PAYMENT_ID);
        return payment;
    }

    private Member createMember() {
        Member member = new Member(
                MEMBER_EMAIL,
                MEMBER_PASSWORD,
                MEMBER_NAME,
                MEMBER_PHONE_NUMBER
        );

        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
        return member;
    }


    @Test
    @DisplayName("결제 확정 성공 - 주문과 결제를 완료 상태로 변경한다")
    void confirm_success() {
        // given
        Member member = createMember();

        Order order = createPendingOrder(member);
        Payment payment = createReadyPayment();
        order.assignPayment(payment);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PORTONE_PAYMENT_ID
        );

        given(orderRepository.findOrderDetailByIdAndMemberId(ORDER_ID, MEMBER_ID))
                .willReturn(Optional.of(order));

        // when
        PaymentConfirmResponse response = paymentService.confirm(MEMBER_ID, request);

        // then
        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.orderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.paidAmount()).isEqualTo(PG_AMOUNT);
        assertThat(response.usedPoint()).isEqualTo(USED_POINT);
        assertThat(response.earnedPoint()).isEqualTo(EARNED_POINT);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 확정 실패 - 주문을 찾을 수 없으면 예외가 발생한다")
    void confirm_fail_orderNotFound() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PORTONE_PAYMENT_ID
        );

        given(orderRepository.findOrderDetailByIdAndMemberId(ORDER_ID, MEMBER_ID))
                .willReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.confirm(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("결제 확정 실패 - 요청한 PortOne 결제 ID가 저장된 결제 ID와 다르면 예외가 발생한다")
    void confirm_fail_portonePaymentIdMismatch() {
        // given
        Member member = createMember();

        Order order = createPendingOrder(member);
        Payment payment = createReadyPayment();
        order.assignPayment(payment);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                "PAY-WRONG-ID"
        );

        given(orderRepository.findOrderDetailByIdAndMemberId(ORDER_ID, MEMBER_ID))
                .willReturn(Optional.of(order));

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.confirm(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제 확정 멱등성 - 이미 결제 완료된 요청은 상태 변경 없이 성공 응답을 반환한다")
    void confirm_success_alreadyPaid() {
        // given
        Member member = createMember();

        Order order = createPendingOrder(member);
        Payment payment = createReadyPayment();
        order.assignPayment(payment);

        order.complete();
        payment.complete();

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                ORDER_ID,
                PORTONE_PAYMENT_ID
        );

        given(orderRepository.findOrderDetailByIdAndMemberId(ORDER_ID, MEMBER_ID))
                .willReturn(Optional.of(order));

        // when
        PaymentConfirmResponse response = paymentService.confirm(MEMBER_ID, request);

        // then
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.paidAmount()).isEqualTo(PG_AMOUNT);
    }
}