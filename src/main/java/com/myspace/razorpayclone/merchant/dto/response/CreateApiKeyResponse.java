package com.myspace.razorpayclone.merchant.dto.response;

import com.myspace.razorpayclone.common.enums.Environment;
import lombok.Data;
import lombok.Getter;

import java.util.UUID;

@Data
public class CreateApiKeyResponse {
    private UUID id;
    private String keyId;
    private String keySecret;
    private Environment environment;
}
