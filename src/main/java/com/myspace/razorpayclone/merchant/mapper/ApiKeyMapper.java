package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.myspace.razorpayclone.merchant.dto.response.CreateApiKeyResponse;
import com.myspace.razorpayclone.merchant.entity.ApiKey;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {
    CreateApiKeyResponse toCreateApiKeyResponse(ApiKey apiKey);
    List<ApiKeyResponse> toListApiKeyResponse(List<ApiKey> apiKeys);
}
