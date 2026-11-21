package com.myspace.razorpayclone.payment.processor.strategy;

import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.payment.processor.PaymentProcessor;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorRequest;
import com.myspace.razorpayclone.payment.processor.dto.PaymentProcessorResponse;

public class WalletPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String WALLET_FAIL_ID = "cust_007";

        String prepaidPaymentInstrument = request.methodDetails() != null ? request.methodDetails().get("ppi").toString() : null;
        if (WALLET_FAIL_ID.equals(prepaidPaymentInstrument)) {
            return new PaymentProcessorResponse.Failure("WALLET_FAILED", "Wallet Transaction Failed");
        }
        String ref = "WALLET_REF_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(ref);
    }
}
