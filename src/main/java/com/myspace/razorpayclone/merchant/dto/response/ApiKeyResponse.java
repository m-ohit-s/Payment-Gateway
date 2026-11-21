package com.myspace.razorpayclone.merchant.dto.response;

import com.myspace.razorpayclone.common.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse (
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        LocalDateTime lastUsedAt,
        LocalDateTime createdAt,
        LocalDateTime rotatedAt
) {
}
