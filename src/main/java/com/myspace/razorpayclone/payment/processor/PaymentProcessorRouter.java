package com.myspace.razorpayclone.payment.processor;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessors;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessors.get(request.paymentMethod());
        if (paymentProcessor == null) {
            throw new IllegalArgumentException("Payment method not supported: " + request.paymentMethod());
        }
        return paymentProcessor.charge(request);
    }
}
