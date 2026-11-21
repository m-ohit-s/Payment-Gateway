package com.myspace.razorpayclone.vault.dto.response;

import com.myspace.razorpayclone.common.enums.CardBrand;

public record TokenizeResponse(
    String token,
    String lastFour,
    CardBrand cardBrand,
    Integer expiryMonth,
    Integer expiryYear
) {
}
