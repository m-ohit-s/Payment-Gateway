package com.myspace.razorpayclone.merchant.controller;

import com.myspace.razorpayclone.merchant.dto.request.CreateApiKeyRequest;
import com.myspace.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.myspace.razorpayclone.merchant.dto.response.CreateApiKeyResponse;
import com.myspace.razorpayclone.merchant.entity.ApiKey;
import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.merchant.service.IApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final IApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest createApiKeyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                apiKeyService.create(merchantContext.getMerchantId(), createApiKeyRequest)
        );
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list() {
        return ResponseEntity.ok(apiKeyService.list(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID keyId) {
        apiKeyService.revoke(merchantContext.getMerchantId(), keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<CreateApiKeyResponse> rotate(@PathVariable UUID keyId) {
        return ResponseEntity.ok().body(
                apiKeyService.rotate(merchantContext.getMerchantId(), keyId)
        );
    }
}
