package com.myspace.razorpayclone.merchant.cache;

import com.myspace.razorpayclone.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry (
        String keyId,
        String keySecretHash,
        String prevKeySecretHash,
        LocalDateTime gracePeriodExpiredAt,
        UUID merchantId,
        Environment environment,
        boolean enabled
) {
    public boolean isInGracePeriod() {
        return enabled && gracePeriodExpiredAt !=null && gracePeriodExpiredAt.isBefore(LocalDateTime.now());
    }
}
