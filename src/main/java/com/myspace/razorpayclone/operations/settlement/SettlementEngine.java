package com.myspace.razorpayclone.operations.settlement;

import com.myspace.razorpayclone.merchant.api.IMerchantLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementEngine {

    private final IMerchantLookupService merchantLookupService;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(cron = "*/30 * * * * *")
    public void runScheduled() {
        log.info("Nightly Settlement Engine started");
        run();
    }

    public void run() {
        List<UUID> merchants = merchantLookupService.listAllActiveMerchants();
        log.info("Found {} merchants: Processing Settlement", merchants.size());
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (UUID merchant : merchants) {
                futures.add(executorService.submit(() -> {
                    // call the settlement executor to process the settlement for this merchant
                    settlementTransactionExecutor.processForMerchant(merchant, LocalDate.now());
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Settlement Batch Future failed ", e);
                    throw new RuntimeException(e);
                }
            }
        }
        log.info("Nightly Settlement Engine finished");
    }

}
