package com.myspace.razorpayclone.merchant.security;

import com.myspace.razorpayclone.common.exceptions.RateLimitException;
import com.myspace.razorpayclone.common.ratelimit.RateLimitResult;
import com.myspace.razorpayclone.common.ratelimit.RateLimiter;
import com.myspace.razorpayclone.merchant.cache.ApiKeyCache;
import com.myspace.razorpayclone.merchant.cache.ApiKeyCacheEntry;
import com.myspace.razorpayclone.merchant.entity.ApiKey;
import com.myspace.razorpayclone.merchant.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-min:60}")
    private int maxRequestsAllowed;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("Incoming request : {}", request.getRequestURI());

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Basic ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String[] credentials = decode(header);
            if (credentials == null || credentials.length != 2) {
                throw new BadCredentialsException("Malinformed API Key Header");
            }

            String apiKey = credentials[0];
            String apiSecret = credentials[1];

//            ApiKey key = apiKeyRepository.findByKeyId(apiKey)
//                    .orElseThrow(() -> new BadCredentialsException("Invalid or Missing API Key"));

            ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(apiKey)
                    .orElseGet(() -> loadAndCache(apiKey));
            if (apiKeyEntry == null || !apiKeyEntry.enabled() || !isSecretValid(apiSecret, apiKeyEntry)) {
                throw new BadCredentialsException("Invalid or Missing API Key");
            }

            RateLimitResult result = rateLimiter.check(
                    "apikey:" + apiKey,
                    maxRequestsAllowed,
                    60
            );

            if (!result.allowed()) {
                log.info("Too many requests: keyId: {}", apiKey);
                throw new RateLimitException("Too many requests", result.retryAfterSeconds());
            }



            var auth = new UsernamePasswordAuthenticationToken(apiKey, null, List.of(
                    new SimpleGrantedAuthority("ROLE_API_KEY")
            ));

            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKeyEntry.merchantId());
            merchantContext.setKeyId(apiKeyEntry.keyId());

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }


    private String[] decode(String header) {
        String encoded = header.substring("Basic ".length()).trim();
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        int colon = decoded.indexOf(':');
        return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
    }

    private boolean isSecretValid(String rawSecret, ApiKeyCacheEntry apiKey) {
        if (passwordEncoder.matches(rawSecret, apiKey.keySecretHash())) {
            return true;
        }
        return apiKey.isInGracePeriod()
                && apiKey.prevKeySecretHash() != null
                && passwordEncoder.matches(rawSecret, apiKey.prevKeySecretHash());
    }

    private ApiKeyCacheEntry loadAndCache(String apiKeyId) {
        ApiKey key = apiKeyRepository.findByKeyId(apiKeyId)
                .orElse(null);
        if (key == null) return null;
        ApiKeyCacheEntry apiKeyCacheEntry = new ApiKeyCacheEntry(
                key.getKeyId(),
                key.getKeySecretHash(),
                key.getPrevKeySecretHash(),
                key.getGracePeriodExpiredAt(),
                key.getMerchant().getId(),
                key.getEnvironment(),
                key.isEnabled()
        );
        apiKeyCache.put(apiKeyId, apiKeyCacheEntry);
        return apiKeyCacheEntry;
    }
}
