package com.myspace.razorpayclone.merchant.dto.request;

import com.myspace.razorpayclone.common.enums.Environment;

public record CreateApiKeyRequest (
        Environment environment
) {

}
