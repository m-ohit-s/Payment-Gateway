package com.myspace.razorpayclone.payment.dto.request;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "order id cannot be null")
        UUID orderId,
        @NotNull(message = "payment method is required")
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails
) {
}
