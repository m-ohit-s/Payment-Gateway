package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.myspace.razorpayclone.merchant.entity.MerchantWebhookConfig;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-18T17:25:25+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.5 (Eclipse Adoptium)"
)
@Component
public class WebhookConfigMapperImpl implements WebhookConfigMapper {

    @Override
    public WebhookConfigResponse toWebhookConfigResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret) {
        if ( merchantWebhookConfig == null && rawSecret == null ) {
            return null;
        }

        UUID id = null;
        String targetUrl = null;
        boolean enabled = false;
        String eventTypes = null;
        if ( merchantWebhookConfig != null ) {
            id = merchantWebhookConfig.getId();
            targetUrl = merchantWebhookConfig.getTargetUrl();
            enabled = merchantWebhookConfig.isEnabled();
            eventTypes = merchantWebhookConfig.getEventTypes();
        }
        String webhookSecret = null;
        webhookSecret = rawSecret;

        WebhookConfigResponse webhookConfigResponse = new WebhookConfigResponse( id, targetUrl, webhookSecret, enabled, eventTypes );

        return webhookConfigResponse;
    }
}
