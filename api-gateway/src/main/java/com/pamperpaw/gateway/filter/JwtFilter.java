package com.pamperpaw.gateway.filter;

import com.pamperpaw.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();
            log.info("Incoming request path: {}", path); // 🔥 DEBUG

            // 🔓 PUBLIC ENDPOINTS
            if (path.startsWith("/auth") || path.startsWith("/customers")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Unauthorized request without token for path={}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token for path={}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String role = jwtUtil.extractRole(token);

            // ✅ ADMIN → full access
            if ("ADMIN".equals(role)) {
                return chain.filter(exchange);
            }

            // ✅ CUSTOMER ACCESS
            if ("CUSTOMER".equals(role)) {

                if (path.startsWith("/customers") ||
                    path.startsWith("/pets") ||
                    path.startsWith("/visit") ||
                    path.startsWith("/vets") ||
                    path.equals("/vets")) {   // 🔥 THIS FIXES YOUR ISSUE

                    return chain.filter(exchange);
                }

                log.warn("Forbidden CUSTOMER access to path={}", path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ✅ VET ACCESS
            if ("VET".equals(role)) {

                if (path.startsWith("/vets") ||
                    path.startsWith("/visit")) {

                    return chain.filter(exchange);
                }

                log.warn("Forbidden VET access to path={}", path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}