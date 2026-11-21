package com.myspace.razorpayclone.operations.webhook;

import com.myspace.razorpayclone.common.enums.WebhookEventStatus;
import com.myspace.razorpayclone.operations.entity.DlqEvent;
import com.myspace.razorpayclone.operations.entity.WebhookEvent;
import com.myspace.razorpayclone.operations.repository.DlqEventRepository;
import com.myspace.razorpayclone.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDlqRecorder {
    private final WebhookEventRepository webhookEventRepository;
    private final DlqEventRepository dlqEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAfterAttemptExhausted(WebhookEvent webhookEvent, String finalError) {
        log.debug("Recording the dlq event with webhook event: {}", webhookEvent.getId());
        webhookEvent.setStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(webhookEvent);
        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(webhookEvent)
                .merchantId(webhookEvent.getMerchantId())
                .finalError(finalError)
                .payload(webhookEvent.getPayload())
                .build();
        dlqEventRepository.save(dlqEvent);
    }

    @Transactional
    public void recordConsumerFailed(ConsumerRecord<String, Map<String, Object>> record, String message) {
        Map<String, Object> envelope = record.value();

        UUID merchantId = null;
        try {
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            Object merchantIdRaw = data!=null ? data.get("merchantId") : null;
            if (merchantIdRaw != null) {
                merchantId = UUID.fromString(merchantIdRaw.toString());
            }

        } catch (Exception ignored) {}
        log.debug("Recording the dlq event because consumer failed event with merchantId: {}", merchantId);
        DlqEvent dlqEvent = DlqEvent.builder()
                .merchantId(merchantId)
                .payload(envelope!=null?envelope:Map.of())
                .webhookEvent(null)
                .finalError(message)
                .build();
        dlqEventRepository.save(dlqEvent);
    }
}
