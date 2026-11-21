package com.myspace.razorpayclone.merchant.service.impls;

import com.myspace.razorpayclone.common.dto.SettlementBankDetails;
import com.myspace.razorpayclone.common.dto.WebhookTarget;
import com.myspace.razorpayclone.common.enums.MerchantStatus;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.merchant.api.IMerchantLookupService;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.merchant.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantLookupService implements IMerchantLookupService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;
    private final MerchantRepository merchantRepository;

    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId)
                .stream().filter(config-> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] cipherText = Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedBytes = bytesEncryptor.decrypt(cipherText);
                    String decryptedSecret = new String(decryptedBytes, StandardCharsets.UTF_8);
                    return new WebhookTarget(
                            config.getId(),
                            config.getTargetUrl(),
                            decryptedSecret
                    );
                })
                .toList();
    }

    @Override
    public List<UUID> listAllActiveMerchants() {
        return merchantRepository.findByMerchantStatus(MerchantStatus.ACTIVE)
                .stream().map(Merchant::getId).toList();
    }

    @Override
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("MERCHANT", merchantId));
        return new SettlementBankDetails(
                merchant.getSettlementBankAccount(),
                merchant.getSettlementBankIfsc(),
                merchant.getSettlementBankAccountHolderName()
        );
    }
}
