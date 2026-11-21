package com.myspace.razorpayclone.merchant.controller;

import com.myspace.razorpayclone.merchant.dto.request.UpdateWebhookConfigRequest;
import com.myspace.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.merchant.service.IWebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {
    private final IWebhookConfigService webhookConfigService;
    private final MerchantContext merchantContext;

    @PostMapping
    ResponseEntity<WebhookConfigResponse> create(@Valid @RequestBody UpdateWebhookConfigRequest configRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                webhookConfigService.create(merchantContext.getMerchantId(), configRequest)
        );
    }

    @GetMapping
    ResponseEntity<List<WebhookConfigResponse>> list() {
        return ResponseEntity.ok(
                webhookConfigService.list(merchantContext.getMerchantId())
        );
    }

    @GetMapping("/{id}")
    ResponseEntity<WebhookConfigResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                webhookConfigService.getById(merchantContext.getMerchantId(), id)
        );
    }

    @PutMapping("/{id}")
    ResponseEntity<WebhookConfigResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.ok(
                webhookConfigService.update(merchantContext.getMerchantId(), id, request)
        );
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        webhookConfigService.delete(merchantContext.getMerchantId(), id);
        return ResponseEntity.noContent().build();
    }
}
