package com.myspace.razorpayclone.merchant.service.impls;

import com.myspace.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.myspace.razorpayclone.common.util.RandomizerUtil;
import com.myspace.razorpayclone.merchant.cache.ApiKeyCache;
import com.myspace.razorpayclone.merchant.dto.request.CreateApiKeyRequest;
import com.myspace.razorpayclone.merchant.dto.response.ApiKeyResponse;
import com.myspace.razorpayclone.merchant.dto.response.CreateApiKeyResponse;
import com.myspace.razorpayclone.merchant.entity.ApiKey;
import com.myspace.razorpayclone.merchant.entity.Merchant;
import com.myspace.razorpayclone.merchant.mapper.ApiKeyMapper;
import com.myspace.razorpayclone.merchant.repository.ApiKeyRepository;
import com.myspace.razorpayclone.merchant.repository.MerchantRepository;
import com.myspace.razorpayclone.merchant.service.IApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyService implements IApiKeyService {
    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ApiKeyCache apiKeyCache;

    @Override
    @Transactional
    public CreateApiKeyResponse create(UUID merchantId, CreateApiKeyRequest createApiKeyRequest) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(
                () -> new ResourceNotFoundException("MERCHANT", merchantId)
        );

        String keyId = "rzp_" + createApiKeyRequest.environment().name().toLowerCase() + RandomizerUtil.randomBase64(24);
        String rawSecret = RandomizerUtil.randomBase64(40);

        ApiKey apiKey = ApiKey.builder()
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(passwordEncoder.encode(rawSecret))
                .environment(createApiKeyRequest.environment())
                .build();
        ApiKey savedKey = apiKeyRepository.save(apiKey);
        CreateApiKeyResponse apiKeyResponse = apiKeyMapper.toCreateApiKeyResponse(savedKey);
        apiKeyResponse.setKeySecret(rawSecret);
        return apiKeyResponse;
    }

    @Override
    public List<ApiKeyResponse> list(UUID merchantId) {
        List<ApiKey> apiKeys = apiKeyRepository.findByMerchant_Id(merchantId);
        return apiKeyMapper.toListApiKeyResponse(apiKeys);
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .filter(key -> key.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("API_KEY", keyId));
        apiKey.setEnabled(false);
        apiKeyCache.evict(apiKey.getKeyId());
    }

    @Override
    @Transactional
    public CreateApiKeyResponse rotate(UUID merchantId, UUID keyId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndMerchant_Id(keyId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("API_KEY", keyId));

        if (!apiKey.isEnabled()) {
            throw new RuntimeException("Cannot rotate disabled api key");
        }
        String newRawSecret = RandomizerUtil.randomBase64(40);
        apiKey.setPrevKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(passwordEncoder.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiredAt(LocalDateTime.now().plusHours(24));
        CreateApiKeyResponse apiKeyResponse = apiKeyMapper.toCreateApiKeyResponse(apiKey);
        apiKeyResponse.setKeySecret(newRawSecret);

        apiKeyCache.evict(apiKey.getKeyId());
        return apiKeyResponse;
    }
}
