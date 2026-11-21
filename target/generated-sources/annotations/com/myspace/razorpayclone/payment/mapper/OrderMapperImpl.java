package com.myspace.razorpayclone.payment.mapper;

import com.myspace.razorpayclone.common.entity.Money;
import com.myspace.razorpayclone.common.enums.OrderStatus;
import com.myspace.razorpayclone.payment.dto.response.OrderResponse;
import com.myspace.razorpayclone.payment.entity.OrderRecord;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toOrderResponse(OrderRecord orderRecord) {
        if ( orderRecord == null ) {
            return null;
        }

        UUID id = null;
        UUID merchantId = null;
        UUID customerId = null;
        String receipt = null;
        Money amount = null;
        OrderStatus orderStatus = null;
        Integer attempts = null;
        Map<String, Object> notes = null;
        LocalDateTime expiresAt = null;
        LocalDateTime createdAt = null;

        id = orderRecord.getId();
        merchantId = orderRecord.getMerchantId();
        customerId = orderRecord.getCustomerId();
        receipt = orderRecord.getReceipt();
        amount = orderRecord.getAmount();
        orderStatus = orderRecord.getOrderStatus();
        attempts = orderRecord.getAttempts();
        Map<String, Object> map = orderRecord.getNotes();
        if ( map != null ) {
            notes = new LinkedHashMap<String, Object>( map );
        }
        expiresAt = orderRecord.getExpiresAt();
        createdAt = orderRecord.getCreatedAt();

        OrderResponse orderResponse = new OrderResponse( id, merchantId, customerId, receipt, amount, orderStatus, attempts, notes, expiresAt, createdAt );

        return orderResponse;
    }
}
