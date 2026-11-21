package com.myspace.razorpayclone.merchant.dto.response;

import com.myspace.razorpayclone.common.enums.MerchantStatus;

public record MerchantActivateResponse (
        MerchantStatus status
) {
}
