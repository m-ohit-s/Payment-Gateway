package com.myspace.razorpayclone.merchant.security;

import com.myspace.razorpayclone.common.exceptions.UnauthorizedException;
import com.myspace.razorpayclone.merchant.service.IAuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final TokenService tokenService;
    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("Incoming request {}", request.getRequestURI());
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                filterChain.doFilter(request, response);
                return;
            }
            String accessToken = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("access-token"))
                    .toList()
                    .getFirst()
                    .getValue();

            if (accessToken == null || accessToken.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.verifyToken(accessToken);
            if (claims == null) {
                handlerExceptionResolver.resolveException(request, response, null,  new UnauthorizedException("INVALID_TOKEN", "Token Invalid"));
                return;
            }
            String tokenVersion = claims.get("tokenVersion").toString();
            Long tokenVersionLong = Long.valueOf(tokenVersion);
            if (!tokenService.validateToken(tokenVersionLong, claims.getSubject())) {
                handlerExceptionResolver.resolveException(request, response, null,  new UnauthorizedException("INVALID_TOKEN_V", "Token Invalid"));
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority(jwtUtil.extractRole(claims)))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                merchantContext.setMerchantId(jwtUtil.extractMerchantId(claims));
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/v1/auth/login") || path.equals("/v1/auth/signup") || path.equals("/v1/auth/refresh");
    }
}
