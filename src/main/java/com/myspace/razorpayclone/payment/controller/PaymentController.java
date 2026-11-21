package com.myspace.razorpayclone.payment.controller;

import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.payment.dto.request.PaymentInitRequest;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final IPaymentService paymentService;
    private final MerchantContext merchantContext;


    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody PaymentInitRequest paymentInitRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        paymentService.initiate(merchantContext.getMerchantId(), paymentInitRequest)
                );
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(
                paymentService.capture(merchantContext.getMerchantId(), paymentId)
        );
    }

}
