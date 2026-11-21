package com.myspace.razorpayclone.payment.processor.strategy;

import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.payment.processor.PaymentProcessor;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;

public class CardPaymentProcessor implements PaymentProcessor {
    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000003";
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        String pan = request.pan();
        if (PAN_CARD_DECLINED.equals(pan)) {
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "card declined by bank");
        }
        if (PAN_CARD_EXPIRED.equals(pan)) {
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "card expired");
        }
        String processorRef = "CARD_PROCESSOR_REF_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
