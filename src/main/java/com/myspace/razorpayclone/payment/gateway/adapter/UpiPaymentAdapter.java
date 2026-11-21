package com.myspace.razorpayclone.payment.gateway.adapter;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.payment.gateway.PaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentResult;
import com.myspace.razorpayclone.payment.processor.PaymentProcessorRouter;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpiPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("initiate paymentRequest with UpiPaymentAdapter: {}", paymentRequest);
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    paymentRequest.paymentId(),
                    paymentRequest.amount(),
                    PaymentMethod.UPI,
                    paymentRequest.methodDetails()
            );

            PaymentProcessorResponse response = paymentProcessorRouter.charge(paymentProcessorRequest);
            return switch (response) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("NetBanking failed with UpiPaymentAdapter: {}", paymentRequest);
            return new PaymentResult.Failure("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_REF");
    }
}
