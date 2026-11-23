package com.myspace.razorpayclone.payment.service.impl;

import com.myspace.razorpayclone.common.enums.PaymentStatus;
import com.myspace.razorpayclone.payment.api.IPaymentLookupService;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupService implements IPaymentLookupService {
    private final PaymentRepository paymentRepository;
    @Override
    @Transactional
    public List<Payment> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
    }

    @Override
    @Transactional
    public void markPaymentsSettled(List<Payment> settledPayments) {
        LocalDateTime now = LocalDateTime.now();
        for (Payment payment : settledPayments) {
            payment.setStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(now);
        }
        paymentRepository.saveAll(settledPayments);
    }
}
