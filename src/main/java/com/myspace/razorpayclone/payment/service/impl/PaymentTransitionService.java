package com.myspace.razorpayclone.payment.service.impl;

import com.myspace.razorpayclone.common.enums.PaymentActor;
import com.myspace.razorpayclone.common.enums.PaymentEvent;
import com.myspace.razorpayclone.common.enums.PaymentStatus;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.entity.PaymentTransitionLog;
import com.myspace.razorpayclone.payment.repository.PaymentTransitionLogRepository;
import com.myspace.razorpayclone.payment.statemachine.PaymentStateMachine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {
    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    @Transactional
    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent) {
        PaymentStatus next = paymentStateMachine.transition(payment.getStatus(), paymentEvent);
        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getStatus())
                .paymentEvent(paymentEvent)
                .toStatus(next)
                .actor(PaymentActor.SYSTEM) //TODO: fetch merchant context to identify actor
                .occurredAt(LocalDateTime.now())
                .build();
        payment.setStatus(next);
        paymentTransitionLogRepository.save(paymentTransitionLog);
        return next;
    }
}
