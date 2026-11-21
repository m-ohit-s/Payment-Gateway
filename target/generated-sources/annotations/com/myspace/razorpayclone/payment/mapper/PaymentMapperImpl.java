package com.myspace.razorpayclone.payment.mapper;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.PaymentMethod;
import com.myspace.razorpayclone.common.enums.PaymentStatus;
import com.myspace.razorpayclone.payment.dto.response.PaymentResponse;
import com.myspace.razorpayclone.payment.entity.Payment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-18T17:25:25+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.5 (Eclipse Adoptium)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public List<PaymentResponse> toPaymentResponse(List<Payment> payment) {
        if ( payment == null ) {
            return null;
        }

        List<PaymentResponse> list = new ArrayList<PaymentResponse>( payment.size() );
        for ( Payment payment1 : payment ) {
            list.add( toPaymentResponse( payment1 ) );
        }

        return list;
    }

    @Override
    public PaymentResponse toPaymentResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentMethod paymentMethod = null;
        PaymentStatus paymentStatus = null;
        UUID id = null;
        UUID merchantId = null;
        Money amount = null;
        Map<String, Object> methodDetails = null;
        String errorCode = null;
        String errorDescription = null;
        LocalDateTime capturedAt = null;
        LocalDateTime createdAt = null;

        paymentMethod = payment.getMethod();
        paymentStatus = payment.getStatus();
        id = payment.getId();
        merchantId = payment.getMerchantId();
        amount = payment.getAmount();
        Map<String, Object> map = payment.getMethodDetails();
        if ( map != null ) {
            methodDetails = new LinkedHashMap<String, Object>( map );
        }
        errorCode = payment.getErrorCode();
        errorDescription = payment.getErrorDescription();
        capturedAt = payment.getCapturedAt();
        createdAt = payment.getCreatedAt();

        UUID orderId = null;
        String cardLastFour = null;
        String cardBrand = null;
        String backReference = null;
        Long refundedAmount = null;

        PaymentResponse paymentResponse = new PaymentResponse( id, orderId, merchantId, amount, paymentStatus, paymentMethod, methodDetails, cardLastFour, cardBrand, backReference, errorCode, errorDescription, refundedAmount, capturedAt, createdAt );

        return paymentResponse;
    }
}
