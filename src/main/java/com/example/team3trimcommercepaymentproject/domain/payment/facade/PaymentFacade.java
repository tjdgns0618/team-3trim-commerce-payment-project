package com.example.team3trimcommercepaymentproject.domain.payment.facade;

import com.example.team3trimcommercepaymentproject.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentConfirmResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.portOne.PortOnePaymentInfo;
import com.example.team3trimcommercepaymentproject.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;

    @Transactional
    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {

        return paymentService.confirm(memberId, request);
    }

    @Transactional
    public void processPortOnePaymentResult(String portonePaymentId, PortOnePaymentInfo paymentInfo){

        paymentService.processPortOnePaymentResult(portonePaymentId, paymentInfo);
    }

}
