package com.myspace.razorpayclone.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.common.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails,
        String cardLastFour,
        String cardBrand,
        String backReference,
        String errorCode,
        String errorDescription,
        Long refundedAmount,
        LocalDateTime capturedAt,
        LocalDateTime createdAt
) {
}
