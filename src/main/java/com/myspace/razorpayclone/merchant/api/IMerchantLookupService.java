package com.myspace.razorpayclone.merchant.api;

import com.myspace.razorpayclone.common.dto.SettlementBankDetails;
import com.myspace.razorpayclone.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface IMerchantLookupService {
    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);

    List<UUID> listAllActiveMerchants();

    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
