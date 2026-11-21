package com.myspace.razorpayclone.payment.mapper;

import com.myspace.razorpayclone.payment.dto.response.OrderResponse;
import com.myspace.razorpayclone.payment.entity.OrderRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(OrderRecord orderRecord);
}
