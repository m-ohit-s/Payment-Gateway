package com.myspace.razorpayclone.payment.gateway.dto;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;


public record PaymentRequest(
        UUID paymentId,
        UUID merchantId,
        UUID orderId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {

}
