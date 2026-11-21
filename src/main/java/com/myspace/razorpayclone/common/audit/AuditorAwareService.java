package com.myspace.razorpayclone.common.audit;

import com.myspace.razorpayclone.merchant.security.MerchantContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAwareService")
@RequiredArgsConstructor
public class AuditorAwareService implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        try {

            String keyId = merchantContext.getKeyId();
            if (keyId != null && !keyId.isBlank()) return Optional.of(keyId);
            if (merchantContext.getMerchantId() != null) {
                return Optional.of("merchant_id: " + merchantContext.getMerchantId());
            }
        } catch (Exception ignored) {

        }
        return Optional.of("SYSTEM");
    }
}
