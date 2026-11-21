package com.myspace.razorpayclone.operations.webhook;

import com.myspace.razorpayclone.common.enums.WebhookEventStatus;
import com.myspace.razorpayclone.operations.entity.WebhookEvent;
import com.myspace.razorpayclone.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryExecutor {
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue webhookRetryQueue;
    private final RestClient restClient;
    private final WebhookDlqRecorder webhookDlqRecorder;

    private static final List<Duration> BACKOFF = List.of(
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(30),
            Duration.ofHours(2), Duration.ofHours(8), Duration.ofHours(24)
    );

    private final int MAX_RETRY_ATTEMPTS = 7;

    @Value("${app.webhook.delivery.signature-header:X-Razorpay-Signature}")
    private String signatureHeader;

    @Transactional
    public void deliver(UUID webhookEventId) {
        Optional<WebhookEvent> webhookEvent = webhookEventRepository.findById(webhookEventId);
        if (webhookEvent.isEmpty()) {
            log.warn("Webhook event with id {} not found", webhookEventId);
            return;
        }
        WebhookEvent event = webhookEvent.get();
        if (event.getStatus().equals(WebhookEventStatus.DELIVERED) || event.getStatus().equals(WebhookEventStatus.DEAD)) {
          log.debug("Webhook event with id {} has been dead or delivered", webhookEventId);
          return;
        }
        event.setAttempts(event.getAttempts() + 1);
        event.setLastAttemptAt(LocalDateTime.now());
        try {
            var response = restClient.post()
                    .uri(event.getTargetUrl())
                    .header(signatureHeader, event.getSignature())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "event", event.getEventType(),
                            "payload", event.getPayload()
                    ))
                    .retrieve()
                    .toBodilessEntity();
            int statusCode = response.getStatusCode().value();
            event.setLastResponseCode(statusCode);

            if (response.getStatusCode().is2xxSuccessful()) {
                event.setStatus(WebhookEventStatus.DELIVERED);
                event.setDeliveredAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                log.info("Successfully delivered webhook event with id {} ", webhookEventId);
                return;
            }
            handleAttemptFailed(event, "HTTP" + statusCode);
        } catch (RestClientException e) {
            event.setLastResponseBody(e.getMessage());
            handleAttemptFailed(event, e.getMessage());
            log.error("RestClientException while delivering webhook event with id {}", webhookEventId, e);
        }
    }

    private void handleAttemptFailed(WebhookEvent event, String message) {
        event.setLastResponseBody(message);
        if (event.getAttempts() >= MAX_RETRY_ATTEMPTS) {
            event.setStatus(WebhookEventStatus.DEAD);
            //Record in Dead Letter Queue after exhaustion
            webhookDlqRecorder.recordAfterAttemptExhausted(event, message);
            return;
        }

        Duration backoff = BACKOFF.get(event.getAttempts() - 1);
        LocalDateTime nextRetryAt = LocalDateTime.now().plus(backoff);
        event.setStatus(WebhookEventStatus.FAILED);
        event.setNextRetryAt(nextRetryAt);
        webhookEventRepository.save(event);

        webhookRetryQueue.enqueue(event.getId(), event.getNextRetryAt());
        log.error("Handling attempt failed webhook event with id {} with attempts {}, Next Retry At : {} ", event.getId(), event.getAttempts(), event.getNextRetryAt());
    }
}
