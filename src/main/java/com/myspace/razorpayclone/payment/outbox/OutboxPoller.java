package com.myspace.razorpayclone.payment.outbox;

import com.myspace.razorpayclone.common.config.KafkaProperties;
import com.myspace.razorpayclone.common.enums.OutboxStatus;
import com.myspace.razorpayclone.payment.entity.OutboxEvent;
import com.myspace.razorpayclone.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxEventHandler outboxEventHandler;

    @Scheduled(fixedRate = 5000)
    public void poll() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        pendingEvents.forEach(pendingEvent -> {
            try {
                String topic = kafkaProperties.topicFor(pendingEvent.getType());
                String key = extractMerchantId(pendingEvent.getPayload());
                Map<String, Object> envelope = Map.of(
                        "eventType", pendingEvent.getEventType(),
                        "aggregateType", pendingEvent.getType().name(),
                        "aggregateId", pendingEvent.getAggregateId(),
                        "data", pendingEvent.getPayload()
                );
                kafkaTemplate.send(topic, key, envelope).get(5, TimeUnit.SECONDS);
                outboxEventHandler.handleEventPublished(pendingEvent);
            } catch (Exception e) {
                log.error("Outbox event failed, eventId: {}, attempts: {}", pendingEvent.getId(), pendingEvent.getAttempts());
                outboxEventHandler.handleEventFailed(pendingEvent, e.getMessage());
            }
        });
    }

    private String extractMerchantId(Map<String, Object> payload) {
        Object value = payload.get("merchantId");
        return value != null ? value.toString() : "unknown";
    }
}
