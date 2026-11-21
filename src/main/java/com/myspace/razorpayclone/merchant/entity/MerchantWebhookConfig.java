package com.myspace.razorpayclone.merchant.entity;

import com.myspace.razorpayclone.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "merchant_webhook_config",
        indexes = {
                @Index(name = "idx_webhook_merchant_id_enabled", columnList = "merchant_id, enabled")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MerchantWebhookConfig extends BaseEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 500)
    private String targetUrl;

    private String webhookSecret;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 255)
    private String eventTypes;

    public boolean isSubscribedTo(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return true;
        }
        String trimmed = eventType.trim();
        String[] eventTypesList = trimmed.split(",");
        for (String event: eventTypesList) {
            if (event.equalsIgnoreCase("ALL") || event.equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }
}
