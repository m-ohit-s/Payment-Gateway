package com.myspace.razorpayclone.payment.outbox;

import com.myspace.razorpayclone.common.enums.OutboxStatus;
import com.myspace.razorpayclone.payment.entity.OutboxEvent;
import com.myspace.razorpayclone.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxEventHandler {
    private final OutboxEventRepository outboxEventRepository;
    private static final Integer MAX_ATTEMPTS = 3;

    @Transactional
    public void handleEventPublished(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxStatus.PUBLISHED);
        outboxEvent.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public void handleEventFailed(OutboxEvent outboxEvent, String error) {
        outboxEvent.setAttempts(outboxEvent.getAttempts() + 1);
        outboxEvent.setLastError(error.length() < 1000 ? error : error.substring(0, 1000));
        if (outboxEvent.getAttempts() >  MAX_ATTEMPTS) {
            outboxEvent.setStatus(OutboxStatus.FAILED);
        }
        outboxEventRepository.save(outboxEvent);
    }
}
