package com.myspace.razorpayclone.payment.service;

import com.myspace.razorpayclone.payment.dto.request.PaymentInitRequest;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface IPaymentService {
    PaymentResponse initiate (UUID merchantId, PaymentInitRequest paymentInitRequest);

    PaymentResponse capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID id, boolean b, String bankRef, String errorCode, String errorDescription);
}
