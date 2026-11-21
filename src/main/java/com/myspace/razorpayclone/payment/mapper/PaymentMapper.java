package com.myspace.razorpayclone.payment.mapper;

import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    List<PaymentResponse> toPaymentResponse(List<Payment> payment);

    @Mapping(source = "method", target = "paymentMethod")
    @Mapping(source = "status", target = "paymentStatus")
    PaymentResponse toPaymentResponse(Payment payment);
}
