package com.myspace.razorpayclone.payment.service.impl;

import com.myspace.razorpayclone.common.enums.EventAggregateType;
import com.myspace.razorpayclone.common.enums.OrderStatus;
import com.myspace.razorpayclone.common.exceptions.BusinessRuleViolationException;
import com.myspace.razorpayclone.common.exceptions.DuplicateResourceException;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.payment.dto.request.CreateOrderRequest;
import com.myspace.razorpayclone.payment.dto.response.OrderResponse;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.entity.OrderRecord;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.mapper.OrderMapper;
import com.myspace.razorpayclone.payment.mapper.PaymentMapper;
import com.myspace.razorpayclone.payment.outbox.OutboxEventPublisher;
import com.myspace.razorpayclone.payment.repository.OrderRepository;
import com.myspace.razorpayclone.payment.repository.PaymentRepository;
import com.myspace.razorpayclone.payment.service.ICustomerService;
import com.myspace.razorpayclone.payment.service.IOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ICustomerService customerService;
    private final OutboxEventPublisher outboxEventPublisher;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest) {
        if (orderRequest.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, orderRequest.receipt())) {
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists");
        }
        UUID customerId = null;

        if (orderRequest.customer() != null) {
            customerId = customerService.findOrCreate(
                    merchantId,
                    orderRequest.customer().email(),
                    orderRequest.customer().name(),
                    orderRequest.customer().phone()
            );
        }
        OrderRecord orderRecord = OrderRecord.builder()
                .merchantId(merchantId)
                .amount(orderRequest.amount())
                .receipt(orderRequest.receipt())
                .notes(orderRequest.notes())
                .customerId(customerId)
                .expiresAt(orderRequest.expiresAt() != null ? orderRequest.expiresAt() : LocalDateTime.now().plusMinutes(defaultExpiryMinutes))
                .build();
        orderRecord = orderRepository.save(orderRecord);
        // Sending outbox
        outboxEventPublisher.publish(
                EventAggregateType.ORDER,
                orderRecord.getId(),
                "ORDER_CREATED",
                Map.of("orderId", orderRecord.getId(),
                        "merchantId", orderRecord.getMerchantId(),
                        "orderStatus", orderRecord.getOrderStatus().name(),
                        "amountUnits", orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency", orderRecord.getAmount().getCurrency()
                )
        );
        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord orderRecord = orderRepository
                .findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", orderId));
        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord orderRecord = orderRepository
                .findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", orderId));
        if (orderRecord.getOrderStatus().equals(OrderStatus.CANCELED) || orderRecord.getOrderStatus().equals(OrderStatus.PAID)) {
            throw new BusinessRuleViolationException("NON_CANCELLABLE_ORDER", "Cannot cancel an order with status: " + orderRecord.getOrderStatus());
        }
        orderRecord.setOrderStatus(OrderStatus.CANCELED);
        orderRecord = orderRepository.save(orderRecord);
        outboxEventPublisher.publish(
                EventAggregateType.ORDER,
                orderRecord.getId(),
                "ORDER_CANCELLED",
                Map.of("orderId", orderRecord.getId(),
                        "merchantId", orderRecord.getMerchantId(),
                        "orderStatus", orderRecord.getOrderStatus().name(),
                        "amountUnits", orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency", orderRecord.getAmount().getCurrency()
                )
        );
        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    public List<PaymentResponse> getByMerchantId(UUID merchantId, UUID orderId) {
        orderRepository
                .findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", orderId));

        List<Payment> paymentList = paymentRepository.findByOrderRecord_Id(orderId);

        return paymentMapper.toPaymentResponse(paymentList);
    }
}
