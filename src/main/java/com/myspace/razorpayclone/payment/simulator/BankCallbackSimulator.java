package com.myspace.razorpayclone.payment.simulator;

import com.myspace.razorpayclone.common.enums.ChaosMode;
import com.myspace.razorpayclone.common.enums.PaymentStatus;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.repository.PaymentRepository;
import com.myspace.razorpayclone.payment.service.impl.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BankCallbackSimulator {
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;


    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks() {
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);
        List<Payment> candidates = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING, globalWindow);
        if (candidates.isEmpty()) {
            return;
        }
        candidates.forEach(this::simulateCallback);
    }

    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig config = simulatorConfig.configFor(payment.getMethod());
        LocalDateTime dueAt = dueAt(payment, config);
        if (dueAt.isAfter(LocalDateTime.now())) {
            return;
        }
        switch (simulatorConfig.getChaosMode()) {
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment, config));
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment, false);
            case TIMEOUT -> {
            }
        }
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig config) {
        int range = config.getMaxDelaySeconds() - config.getMinDelaySeconds();
        int delaySeconds = config.getMinDelaySeconds() + Math.abs(payment.getId().hashCode() % (range+1));

        if (simulatorConfig.getChaosMode().equals(ChaosMode.SLOW)) {
            delaySeconds *= 2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }

    private void resolve(Payment payment, boolean approve) {
        if (approve) {
            String bankRef = "SIM_BANK_REF_" + RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, bankRef, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE", "Simulated Bank declined");
        }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig config) {
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return bucket < config.getSuccessRate();
    }
}
