package com.myspace.razorpayclone.payment.processor.strategy;

import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.payment.processor.PaymentProcessor;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;

public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@upi";
        String upiCode = request.methodDetails() != null ? request.methodDetails().get("vpa").toString() : null;
        if (VPA_CODE_FAIL.equals(upiCode)) {
            return new PaymentProcessorResponse.Failure("UPI_FAILED", "UPI Rejected the Payment");
        }
        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
