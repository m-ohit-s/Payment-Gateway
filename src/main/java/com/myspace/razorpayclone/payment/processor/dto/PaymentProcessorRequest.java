package com.myspace.razorpayclone.payment.processor.dto;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID paymentId,
        UUID processingId,
        PaymentMethod paymentMethod,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
) {

    public static PaymentProcessorRequest card(
            UUID paymentId,
            String pan,
            String expiry,
            Money amount,
            Map<String, Object> methodDetails
    ) {
        return new PaymentProcessorRequest(
                paymentId,
                UUID.randomUUID(),
                PaymentMethod.CARD,
                amount,
                pan,
                expiry,
                methodDetails
        );
    }

    public static PaymentProcessorRequest nonCard(
            UUID paymentId,
            Money amount,
            PaymentMethod paymentMethod,
            Map<String, Object> methodDetails
    ) {
        return new PaymentProcessorRequest(
                paymentId,
                UUID.randomUUID(),
                paymentMethod,
                amount,
                null,
                null,
                methodDetails
        );
    }
}
