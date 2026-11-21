package com.myspace.razorpayclone.payment.config;

import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.payment.gateway.PaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.adapter.CardPaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.adapter.NetBankingPaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.adapter.UpiPaymentAdapter;
import com.myspace.razorpayclone.payment.gateway.adapter.WalletPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {
    private final NetBankingPaymentAdapter netBankingPaymentAdapter;
    private final WalletPaymentAdapter walletPaymentAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {

        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.UPI, upiPaymentAdapter,
                PaymentMethod.NET_BANKING, netBankingPaymentAdapter,
                PaymentMethod.WALLET, walletPaymentAdapter
        );
    }
}
