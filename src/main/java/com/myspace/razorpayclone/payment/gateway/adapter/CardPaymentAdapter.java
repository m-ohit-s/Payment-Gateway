package com.myspace.razorpayclone.payment.gateway.adapter;

import com.myspace.razorpayclone.payment.gateway.PaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentResult;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;
import com.myspace.razorpayclone.vault.service.IVaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final IVaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        String token = paymentRequest.methodDetails().get("token").toString();
        PaymentProcessorResponse response = vaultService.charge(paymentRequest.paymentId(), token, paymentRequest.amount(), paymentRequest.methodDetails());
        return switch (response) {
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
