package com.myspace.razorpayclone.payment.gateway;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiate(PaymentRequest paymentRequest) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(paymentRequest.method());
        if (paymentAdapter == null) {
            throw new IllegalArgumentException("Invalid PaymentRequest method " + paymentRequest.method());
        }
        return paymentAdapter.initiate(paymentRequest);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(method);
        if (paymentAdapter == null) {
            throw new IllegalArgumentException("Invalid PaymentRequest method " + method);
        }
        return paymentAdapter.capture(paymentId);
    }
}
