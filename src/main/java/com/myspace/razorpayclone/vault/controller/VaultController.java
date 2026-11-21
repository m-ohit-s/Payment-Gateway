package com.myspace.razorpayclone.vault.controller;

import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.vault.dto.request.TokenizeRequest;
import com.myspace.razorpayclone.vault.dto.response.TokenizeResponse;
import com.myspace.razorpayclone.vault.service.IVaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final IVaultService vaultService;
    private final MerchantContext merchantContext;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@Valid @RequestBody TokenizeRequest tokenizeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                vaultService.tokenize(merchantContext.getMerchantId(), tokenizeRequest)
        );
    }
}
