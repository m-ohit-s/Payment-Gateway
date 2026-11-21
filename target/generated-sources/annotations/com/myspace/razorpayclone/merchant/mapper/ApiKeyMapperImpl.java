package com.myspace.razorpayclone.merchant.mapper;

import com.myspace.razorpayclone.common.enums.Environment;
import com.myspace.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.myspace.razorpayclone.merchant.dto.response.CreateApiKeyResponse;
import com.myspace.razorpayclone.merchant.entity.ApiKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-18T17:25:25+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.5 (Eclipse Adoptium)"
)
@Component
public class ApiKeyMapperImpl implements ApiKeyMapper {

    @Override
    public CreateApiKeyResponse toCreateApiKeyResponse(ApiKey apiKey) {
        if ( apiKey == null ) {
            return null;
        }

        CreateApiKeyResponse createApiKeyResponse = new CreateApiKeyResponse();

        createApiKeyResponse.setId( apiKey.getId() );
        createApiKeyResponse.setKeyId( apiKey.getKeyId() );
        createApiKeyResponse.setEnvironment( apiKey.getEnvironment() );

        return createApiKeyResponse;
    }

    @Override
    public List<ApiKeyResponse> toListApiKeyResponse(List<ApiKey> apiKeys) {
        if ( apiKeys == null ) {
            return null;
        }

        List<ApiKeyResponse> list = new ArrayList<ApiKeyResponse>( apiKeys.size() );
        for ( ApiKey apiKey : apiKeys ) {
            list.add( apiKeyToApiKeyResponse( apiKey ) );
        }

        return list;
    }

    protected ApiKeyResponse apiKeyToApiKeyResponse(ApiKey apiKey) {
        if ( apiKey == null ) {
            return null;
        }

        UUID id = null;
        String keyId = null;
        Environment environment = null;
        boolean enabled = false;
        LocalDateTime lastUsedAt = null;
        LocalDateTime createdAt = null;
        LocalDateTime rotatedAt = null;

        id = apiKey.getId();
        keyId = apiKey.getKeyId();
        environment = apiKey.getEnvironment();
        enabled = apiKey.isEnabled();
        lastUsedAt = apiKey.getLastUsedAt();
        createdAt = apiKey.getCreatedAt();
        rotatedAt = apiKey.getRotatedAt();

        ApiKeyResponse apiKeyResponse = new ApiKeyResponse( id, keyId, environment, enabled, lastUsedAt, createdAt, rotatedAt );

        return apiKeyResponse;
    }
}
