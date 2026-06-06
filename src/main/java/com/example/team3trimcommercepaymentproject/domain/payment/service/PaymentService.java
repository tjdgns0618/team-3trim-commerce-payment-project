package com.example.team3trimcommercepaymentproject.domain.payment.service;

import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOneClient;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOnePaymentInfo;
import com.example.team3trimcommercepaymentproject.domain.payment.repository.PaymentRepository;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.service.PointTransactionService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {


    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PointTransactionService pointTransactionService;
    private final PortOneClient portOneClient;


    /**
     * 결제 확정
     **/
    @Transactional
    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest confirmRequest) {

        Long orderId = confirmRequest.orderId();
        String portonePaymentId = confirmRequest.portonePaymentId();

        Order order = orderRepository.findOrderDetailByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = order.getPayment();

        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!payment.getPortonePaymentId().equals(portonePaymentId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (payment.isPaid()) {
            return PaymentConfirmResponse.from(order, payment);
        }

        PortOnePaymentInfo paymentInfo = portOneClient.getPayment(portonePaymentId);

        if (!paymentInfo.isPaid()) {
            cancelOrderBecausePaymentFailed(order, payment);

            orderRepository.save(order);
            paymentRepository.save(payment);

            return PaymentConfirmResponse.from(order, payment);
        }

        validateAmount(payment, paymentInfo);

        PaymentConfirmResponse response = finalizePayment(order, payment);

        applyPointTransactions(order, payment);

        orderRepository.save(order);
        paymentRepository.save(payment);

        return response;
    }


    /**
     * 웹훅수신
     **/

    @Transactional
    public void processPortOnePaymentResult(String portonePaymentId, PortOnePaymentInfo paymentInfo) {

        Payment payment = paymentRepository.findByPortonePaymentId(portonePaymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        if (payment.isPaid()) {
            return;
        }

        if (paymentInfo.isPaid()) {
            validateAmount(payment, paymentInfo);
            finalizePayment(order, payment);


            applyPointTransactions(order, payment);

            orderRepository.save(order);
            paymentRepository.save(payment);

            return;
        }

        if (paymentInfo.isFailed()) {
            cancelOrderBecausePaymentFailed(order, payment);

            orderRepository.save(order);
            paymentRepository.save(payment);

            return;
        }
        throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
    }


    private PaymentConfirmResponse finalizePayment(Order order, Payment payment) {
        if (payment.isPaid()) {
            return PaymentConfirmResponse.from(order, payment);
        }

        order.complete();
        payment.complete();

        return PaymentConfirmResponse.from(order, payment);
    }

    private void cancelOrderBecausePaymentFailed(Order order, Payment payment) {
        for (OrderItem orderItem : order.getOrderItems()) {

            Product product = orderItem.getProduct();

            product.increaseStock(orderItem.getQuantity());
        }

        order.cancel("결제 실패");
        payment.fail();
    }

    private void validateAmount(Payment payment, PortOnePaymentInfo paymentInfo) {
        if (!payment.getPgAmount().equals(paymentInfo.paidAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void applyPointTransactions(Order order, Payment payment) {
        if (payment.getUsedPoint() != null && payment.getUsedPoint() > 0L) {
            pointTransactionService.usePoint(order.getMember().getId(), payment, payment.getUsedPoint());
        }

        if (payment.getEarnedPoint() != null && payment.getEarnedPoint() > 0L) {
            pointTransactionService.earnPoint(
                    order.getMember().getId(), payment, payment.getEarnedPoint()
            );
        }
    }

}


