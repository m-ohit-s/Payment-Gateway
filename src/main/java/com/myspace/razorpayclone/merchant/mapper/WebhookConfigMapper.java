package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.merchant.dto.response.WebhookConfigResponse;
import com.myspace.razorpayclone.merchant.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookConfigMapper {
    @Mapping(target = "webhookSecret", source = "rawSecret")
    WebhookConfigResponse toWebhookConfigResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret);
}
