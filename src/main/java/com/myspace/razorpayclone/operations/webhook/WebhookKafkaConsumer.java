package com.myspace.razorpayclone.operations.webhook;

import com.myspace.razorpayclone.common.dto.WebhookTarget;
import com.myspace.razorpayclone.common.enums.WebhookEventStatus;
import com.myspace.razorpayclone.common.util.SignerUtil;
import com.myspace.razorpayclone.merchant.api.IMerchantLookupService;
import com.myspace.razorpayclone.operations.entity.WebhookEvent;
import com.myspace.razorpayclone.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookKafkaConsumer {

    private final IMerchantLookupService merchantLookupService;
    private final ObjectMapper objectMapper;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue webhookRetryQueue;
    private final WebhookDlqRecorder dlqRecorder;

    @KafkaListener(
            topics = {
                    "${app.kafka.topics.payment:payment.events}",
                    "${app.kafka.topics.order:order.events}",
                    "${app.kafka.topics.refund:refund.events}",
                    "${app.kafka.topics.settlement:settlement.events}"
            }
    )
    public void onWebhookEvent(ConsumerRecord<String, Map<String, Object>> record, Acknowledgment acknowledgement) {
        try {
            Map<String, Object> envelope = record.value();
            Map<String, Object> data = (Map<String, Object>) envelope.get("data");
            String eventType = envelope.get("eventType").toString();
            Object merchantIdRaw = data.get("merchantId");
            if (merchantIdRaw == null) {
                log.warn("No merchant id found for event type {} skipping the event", eventType);
                acknowledgement.acknowledge();
                return;
            }
            UUID merchantId = UUID.fromString(merchantIdRaw.toString());
            List<WebhookTarget> webhookTargets = merchantLookupService.getActiveConfigsForEvent(merchantId, eventType);
            if (webhookTargets.isEmpty()) {
                acknowledgement.acknowledge();
                log.debug("No webhook target present");
                return;
            }
            Map<String, Object> signatureData = Map.of(
                    "event", eventType,
                    "payload", data
            );

            String signatureJson = objectMapper.writeValueAsString(signatureData);
            for (WebhookTarget webhookTarget : webhookTargets) {
                String signature = SignerUtil.sign(signatureJson, webhookTarget.webhookSecret());
                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(webhookTarget.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();
                webhookEvent = webhookEventRepository.save(webhookEvent);
                webhookRetryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
                log.info("Created webhook event with id {}", webhookEvent.getId());
            }
            acknowledgement.acknowledge();
        } catch (DataAccessException | CannotCreateTransactionException dbDown) {
           log.error("Webhook consumer failed to due to Database Down record, offset: {}", record.offset());
        } catch (Exception logicError) {
            dlqRecorder.recordConsumerFailed(record, logicError.getMessage());
            acknowledgement.acknowledge();
        }
    }
}
