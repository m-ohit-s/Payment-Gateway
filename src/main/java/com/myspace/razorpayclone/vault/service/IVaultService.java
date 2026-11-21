package com.myspace.razorpayclone.vault.service;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;
import com.myspace.razorpayclone.vault.dto.request.TokenizeRequest;
import com.myspace.razorpayclone.vault.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface IVaultService {
    TokenizeResponse tokenize(UUID merchantId, TokenizeRequest tokenizeRequest);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
