package com.myspace.razorpayclone.payment.outbox;

import com.myspace.razorpayclone.common.enums.EventAggregateType;
import com.myspace.razorpayclone.payment.entity.OutboxEvent;
import com.myspace.razorpayclone.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final OutboxEventRepository outboxEventRepository;

    public void publish(EventAggregateType eventAggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .type(eventAggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
