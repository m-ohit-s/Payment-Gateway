package com.myspace.razorpayclone.payment.gateway;

import com.myspace.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {
    PaymentResult initiate(PaymentRequest paymentRequest);

    PaymentResult capture(UUID paymentId);
}
