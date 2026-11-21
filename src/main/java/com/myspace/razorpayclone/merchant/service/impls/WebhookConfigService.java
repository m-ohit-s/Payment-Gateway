package com.myspace.razorpayclone.merchant.service.impls;

import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.merchant.dto.request.UpdateWebhookConfigRequest;
import com.myspace.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.entity.MerchantWebhookConfig;
import com.myspace.razorpayclone.merchant.mapper.WebhookConfigMapper;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.merchant.repository.WebhookConfigRepository;
import com.myspace.razorpayclone.merchant.service.IWebhookConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookConfigService implements IWebhookConfigService {
    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    @Transactional
    public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest updateWebhookConfigRequest) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MERCHANT", merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        String encryptedSecret = Base64.getEncoder().encodeToString(bytesEncryptor.encrypt(rawSecretBytes));
        MerchantWebhookConfig merchantWebhookConfig = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(updateWebhookConfigRequest.targetUrl())
                .webhookSecret(encryptedSecret)
                .enabled(true)
                .eventTypes(updateWebhookConfigRequest.eventTypes())
                .build();

        merchantWebhookConfig = webhookConfigRepository.save(merchantWebhookConfig);

        return webhookConfigMapper.toWebhookConfigResponse(merchantWebhookConfig, rawSecret);
    }

    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId)
                .stream()
                .map(config -> webhookConfigMapper.toWebhookConfigResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigMapper.toWebhookConfigResponse(requireOwnedConfig(merchantId, webhookConfigId), null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID webhookConfigId, UpdateWebhookConfigRequest updateWebhookConfigRequest) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, webhookConfigId);
        config.setTargetUrl(updateWebhookConfigRequest.targetUrl());
        config.setEventTypes(updateWebhookConfigRequest.eventTypes());
        return webhookConfigMapper.toWebhookConfigResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID webhookConfigId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, webhookConfigId);
        webhookConfigRepository.delete(config);
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigRepository.findByIdAndMerchant_Id(webhookConfigId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("WEBHOOK_CONFIG", webhookConfigId));
    }
}
