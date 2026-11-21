package com.myspace.razorpayclone.payment.service;

import com.myspace.razorpayclone.payment.dto.request.CreateOrderRequest;
import com.myspace.razorpayclone.payment.dto.response.OrderResponse;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface IOrderService {

    OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest);

    OrderResponse getById(UUID merchantId, UUID orderId);

    OrderResponse cancel(UUID merchantId, UUID orderId);

    List<PaymentResponse> getByMerchantId(UUID merchantId, UUID orderId);
}
