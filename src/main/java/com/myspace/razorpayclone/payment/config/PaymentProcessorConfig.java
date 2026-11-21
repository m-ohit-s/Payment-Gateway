package com.myspace.razorpayclone.payment.config;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.payment.processor.PaymentProcessor;
import com.myspace.razorpayclone.payment.processor.strategy.CardPaymentProcessor;
import com.myspace.razorpayclone.payment.processor.strategy.NetBankingPaymentProcessor;
import com.myspace.razorpayclone.payment.processor.strategy.UpiPaymentProcessor;
import com.myspace.razorpayclone.payment.processor.strategy.WalletPaymentProcessor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessors() {
        return Map.of(
            PaymentMethod.CARD, new CardPaymentProcessor(),
            PaymentMethod.WALLET, new WalletPaymentProcessor(),
            PaymentMethod.NET_BANKING, new NetBankingPaymentProcessor(),
            PaymentMethod.UPI, new UpiPaymentProcessor()
        );
    }
}
