package com.myspace.razorpayclone.payment.controller;

import com.myspace.razorpayclone.merchant.security.MerchantContext;
import com.myspace.razorpayclone.payment.dto.request.CreateOrderRequest;
import com.myspace.razorpayclone.payment.dto.response.OrderResponse;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.service.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final MerchantContext merchantContext;


    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest orderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                orderService.create(merchantContext.getMerchantId(), orderRequest)
        );
    }

    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> findById(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                orderService.getById(merchantContext.getMerchantId(), orderId)
        );
    }

    @PostMapping("/{orderId}/cancel")
    ResponseEntity<OrderResponse> cancel(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                orderService.cancel(merchantContext.getMerchantId(), orderId)
        );
    }

    @GetMapping("/{orderId}/payments")
    ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                orderService.getByMerchantId(merchantContext.getMerchantId(), orderId)
        );
    }
}
