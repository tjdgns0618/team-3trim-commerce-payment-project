package com.example.team3trimcommercepaymentproject.domain.payment.handler;

import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class PortOneWebhookVerifer {

    public void verify(String signatuer, String body){
        if (body == null || body.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

}
