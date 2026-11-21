package com.myspace.razorpayclone.payment.service.impl;

import com.myspace.razorpayclone.common.enums.EventAggregateType;
import com.myspace.razorpayclone.common.enums.OrderStatus;
import com.myspace.razorpayclone.common.enums.PaymentEvent;
import com.myspace.razorpayclone.common.enums.PaymentStatus;
import com.myspace.razorpayclone.common.exceptions.BusinessRuleViolationException;
import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.payment.dto.request.PaymentInitRequest;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.entity.OrderRecord;
import com.myspace.razorpayclone.payment.entity.Payment;
import com.myspace.razorpayclone.payment.gateway.PaymentGatewayRouter;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentRequest;
import com.myspace.razorpayclone.payment.gateway.dto.PaymentResult;
import com.myspace.razorpayclone.payment.mapper.PaymentMapper;
import com.myspace.razorpayclone.payment.outbox.OutboxEventPublisher;
import com.myspace.razorpayclone.payment.repository.OrderRepository;
import com.myspace.razorpayclone.payment.repository.PaymentRepository;
import com.myspace.razorpayclone.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher outboxEventPublisher;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest paymentInitRequest) {
//        OrderRecord order = orderRepository.findByIdAndMerchantId(paymentInitRequest.orderId(), merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", paymentInitRequest.orderId()));

        OrderRecord order = orderRepository.findByIdAndMerchantIdForUpdate(paymentInitRequest.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND", paymentInitRequest.orderId()));

        if (!order.getOrderStatus().equals(OrderStatus.CREATED) && !order.getOrderStatus().equals(OrderStatus.ATTEMPTED)) {
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "order cannot be initiated with status: " + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment = Payment.builder()
                .orderRecord(order)
                .merchantId(merchantId)
                .amount(order.getAmount())
                .status(PaymentStatus.CREATED)
                .method(paymentInitRequest.paymentMethod())
                .idempotencyKey(UUID.randomUUID().toString()) //TODO: Need to change this
                .methodDetails(paymentInitRequest.methodDetails())
                .build();

        payment = paymentRepository.save(payment);

        PaymentRequest paymentRequest = new PaymentRequest(
                payment.getId(),
                merchantId,
                paymentInitRequest.orderId(),
                order.getAmount(),
                paymentInitRequest.paymentMethod(),
                paymentInitRequest.methodDetails()
        );

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);

        PaymentResult paymentResult = paymentGatewayRouter.initiate(paymentRequest);

        switch (paymentResult) {
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Success success -> payment.setBankReferenceNumber(success.bankReference());
        }
        paymentRepository.save(payment);
        orderRepository.save(order);

        // Outbox setup
        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_CREATED",
                Map.of("orderId", order.getId(),
                        "paymentId", payment.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );

        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
//        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId, merchantId)
//                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", paymentId));

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT_NOT_FOUND", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);
        switch (paymentResult) {
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Pending pending -> paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            case PaymentResult.Success success -> {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
            }
        }
        // TODO: Send an outbox (kafka event)
        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrderRecord().getId(),
                        "paymentId", payment.getId(),
                        "merchantId", merchantId.toString(),
                        "orderStatus", payment.getOrderRecord().getOrderStatus().name(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits", payment.getOrderRecord().getAmount().getAmountUnits(),
                        "amountCurrency", payment.getOrderRecord().getAmount().getCurrency()
                )
        );
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID id, boolean approve, String bankRef, String errorCode, String errorDescription) {

//        Payment payment = paymentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", id));

        Payment payment = paymentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", id));

        if (!payment.getStatus().equals(PaymentStatus.AUTHORIZING)) {
            return;
        }
        OrderRecord order = payment.getOrderRecord();
        if (approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReferenceNumber(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getMethod(), payment.getId());
            if (captureResult instanceof PaymentResult.Success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                order.setOrderStatus(OrderStatus.PAID);
            } else {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(errorCode);
                payment.setErrorDescription(errorDescription);
            }
        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
        // TODO: Send an outbox (kafka event)
        outboxEventPublisher.publish(
                EventAggregateType.PAYMENT,
                payment.getId(),
                "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrderRecord().getId(),
                        "paymentId", payment.getId(),
                        "merchantId", payment.getOrderRecord().getMerchantId().toString(),
                        "orderStatus", payment.getOrderRecord().getOrderStatus().name(),
                        "paymentStatus",payment.getStatus().name(),
                        "amountUnits", payment.getOrderRecord().getAmount().getAmountUnits(),
                        "amountCurrency", payment.getOrderRecord().getAmount().getCurrency()
                )
        );
    }
}
