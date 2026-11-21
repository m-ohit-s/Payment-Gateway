package com.myspace.razorpayclone.merchant.service;

import com.myspace.razorpayclone.merchant.dto.request.UpdateWebhookConfigRequest;
import com.myspace.razorpayclone.merchant.dto.response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface IWebhookConfigService {
    WebhookConfigResponse create (UUID merchantId, UpdateWebhookConfigRequest updateWebhookConfigRequest);
    List<WebhookConfigResponse> list(UUID merchantId);
    WebhookConfigResponse getById(UUID merchantId, UUID webhookConfigId);
    WebhookConfigResponse update(UUID merchantId, UUID webhookConfigId, UpdateWebhookConfigRequest updateWebhookConfigRequest);
    void delete(UUID merchantId, UUID webhookConfigId);
}
