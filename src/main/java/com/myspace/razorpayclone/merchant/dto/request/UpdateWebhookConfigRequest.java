package com.myspace.razorpayclone.merchant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookConfigRequest (
        @NotBlank(message = "target url cannot be blank")
        @Size(max = 500)
        @Pattern(regexp = "^https?://.+", message = "Webhook url must be a valid http(s) URL")
        String targetUrl,


        // Comma-Separated fine-grained event type names (e.g. "PAYMENT_STATUS_CHANGED,REFUND_CREATED").
        // Null/Blank/"ALL" subscribes to every event type
        @Size(max = 1000)
        String eventTypes
) {
}
