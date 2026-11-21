package com.myspace.razorpayclone.operations.settlement;

import com.myspace.razorpayclone.common.enums.ChaosMode;
import com.myspace.razorpayclone.common.enums.SettlementStatus;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.operations.entity.Settlement;
import com.myspace.razorpayclone.operations.repository.SettlementRepository;
import com.myspace.razorpayclone.operations.settlement.simulator.SettlementSimulatorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementCallbackSimulator {
    private final SettlementSimulatorConfig settlementSimulatorConfig;
    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(fixedDelayString = "${settlement.simulator.poll-interval-ms:5000}")
    public void processCallbacks() {
        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);
        List<Settlement> candidates = settlementRepository.findByStatusAndCreatedAtBefore(SettlementStatus.TRANSFER_PENDING, globalWindow);
        if (candidates.isEmpty()) {
            return;
        }
        candidates.forEach(this::simulateSettlement);
    }

    private void simulateSettlement(Settlement settlement) {
        LocalDateTime dueAt = dueAt(settlement);
        if (dueAt.isAfter(LocalDateTime.now())) {
            return;
        }
        switch (settlementSimulatorConfig.getChaosMode()) {
            case NORMAL, SLOW -> resolveSettlement(settlement, shouldApprove(settlement));
            case SUCCESS -> resolveSettlement(settlement, true);
            case FAILURE -> resolveSettlement(settlement, false);
        }
    }

    private LocalDateTime dueAt(Settlement settlement) {
        int range = settlementSimulatorConfig.getMaxDelaySeconds() - settlementSimulatorConfig.getMinDelaySeconds();
        int delaySeconds = settlementSimulatorConfig.getMinDelaySeconds() + Math.abs(settlement.getId().hashCode() % (range + 1));
        if (ChaosMode.SLOW.equals(settlementSimulatorConfig.getChaosMode())) {
            delaySeconds *= 2;
        }
        return settlement.getCreatedAt().plusSeconds(delaySeconds);
    }

    private void resolveSettlement(Settlement settlement, boolean approve) {
        if (approve) {
            settlementTransactionExecutor.resolveTransfer(settlement.getId(), null, null);
        } else {
            settlementTransactionExecutor.resolveTransfer(settlement.getId(), "SIM_SETTLEMENT_BANK_ERROR_CODE", "Simulated Settlement Bank declined");
        }
    }

    private boolean shouldApprove(Settlement settlement) {
        int bucket = Math.abs(settlement.getId().hashCode()) % 100;
        return bucket < settlementSimulatorConfig.getSuccessRate();
    }
 }
