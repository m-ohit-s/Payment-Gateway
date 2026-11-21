package com.myspace.razorpayclone.operations.webhook;

import com.myspace.razorpayclone.common.enums.WebhookEventStatus;
import com.myspace.razorpayclone.operations.entity.WebhookEvent;
import com.myspace.razorpayclone.operations.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryScheduler {

    private final WebhookRetryQueue webhookRetryQueue;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookDeliveryExecutor deliveryExecutor;
    private ExecutorService virtualThreadExecutor;

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int BATCH_SIZE;

    @PostConstruct
    public void init() {
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    public void shutdown() {
        virtualThreadExecutor.shutdown();
    }

    @Scheduled(fixedDelay = 1000)
    public void pollAndDeliver() {
        Set<UUID> due = webhookRetryQueue.pollDue(BATCH_SIZE);
        if (due.isEmpty()) {
            return;
        }
        for (UUID webhookEventId : due) {
            virtualThreadExecutor.submit(() -> deliveryExecutor.deliver(webhookEventId));
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void reconcileFromDatabase() {
        LocalDateTime now = LocalDateTime.now();
        List<WebhookEvent> due = webhookEventRepository.findByStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING, now);
        for (WebhookEvent webhookEvent : due) {
            webhookRetryQueue.enqueueIfAbsent(webhookEvent.getId(), webhookEvent.getNextRetryAt());
        }
    }
}
