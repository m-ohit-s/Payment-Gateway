package com.myspace.razorpayclone.merchant.service;

import com.myspace.razorpayclone.merchant.dto.request.CreateApiKeyRequest;
import com.myspace.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.myspace.razorpayclone.merchant.dto.response.CreateApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface IApiKeyService {
    CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest createApiKeyRequest);

    List<ApiKeyResponse> list(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    CreateApiKeyResponse rotate(UUID merchantId, UUID keyId);
}
