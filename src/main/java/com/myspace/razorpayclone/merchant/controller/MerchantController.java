package com.myspace.razorpayclone.merchant.controller;

import com.myspace.razorpayclone.merchant.dto.request.MerchantActivateRequest;
import com.myspace.razorpayclone.merchant.dto.response.MerchantActivateResponse;
import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.merchant.service.IMerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/merchants")
public class MerchantController {

    private final MerchantContext merchantContext;
    private final IMerchantService merchantService;

    @PostMapping("/activate")
    ResponseEntity<MerchantActivateResponse> activateMerchant(@RequestBody MerchantActivateRequest merchantActivateRequest) {
        UUID merchantId = merchantContext.getMerchantId();
        return ResponseEntity.ok(merchantService.activateMerchant(merchantId, merchantActivateRequest));
    }
}
