package com.pamperpaw.gateway.filter;

import com.pamperpaw.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Key";

    private final JwtUtil jwtUtil;
    private final String internalServiceKey;

    public JwtFilter(JwtUtil jwtUtil,
                     @Value("${internal.service.key}") String internalServiceKey) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.internalServiceKey = internalServiceKey;
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();
            log.info("Incoming request method={} path={}", method, path);

            if (HttpMethod.OPTIONS.equals(method)) {
                return chain.filter(withInternalServiceHeader(exchange.getRequest(), exchange));
            }

            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Unauthorized request without token for method={} path={}", method, path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token for method={} path={}", method, path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String role = jwtUtil.extractRole(token);
            String username = jwtUtil.extractUsername(token);
            if (!isAuthorized(role, method, path)) {
                log.warn("Forbidden role={} access to method={} path={}", role, method, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(withGatewayHeaders(exchange.getRequest(), exchange, username, role));
        };
    }

    private ServerWebExchange withInternalServiceHeader(ServerHttpRequest request, ServerWebExchange exchange) {
        ServerHttpRequest requestWithInternalHeader = request
                .mutate()
                .header(INTERNAL_SERVICE_HEADER, internalServiceKey)
                .build();

        return exchange.mutate().request(requestWithInternalHeader).build();
    }

    private ServerWebExchange withGatewayHeaders(ServerHttpRequest request,
                                                 ServerWebExchange exchange,
                                                 String username,
                                                 String role) {
        ServerHttpRequest requestWithHeaders = request
                .mutate()
                .header(INTERNAL_SERVICE_HEADER, internalServiceKey)
                .header("X-User-Name", username)
                .header("X-User-Role", role)
                .build();

        return exchange.mutate().request(requestWithHeaders).build();
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/")
                || path.equals("/v3/api-docs")
                || path.startsWith("/v3/api-docs/");
    }

    private boolean isAuthorized(String role, HttpMethod method, String path) {
        if ("ADMIN".equals(role)) {
            return path.startsWith("/admin")
                    || path.startsWith("/customers")
                    || path.startsWith("/pets")
                    || path.startsWith("/vets")
                    || path.startsWith("/visit")
                    || path.startsWith("/visits")
                    || path.startsWith("/payments");
        }

        if ("CUSTOMER".equals(role)) {
            return isCustomerAccess(method, path);
        }

        if ("VET".equals(role)) {
            return isVetAccess(method, path);
        }

        return false;
    }

    private boolean isCustomerAccess(HttpMethod method, String path) {
        if (HttpMethod.GET.equals(method)) {
            return path.startsWith("/customers/username/")
                    || path.startsWith("/pets/customer/")
                    || path.equals("/vets")
                    || path.startsWith("/vets/")
                    || path.startsWith("/visit/customer/")
                    || path.startsWith("/visits/customer/")
                    || path.matches("/visit/vet/\\d+/unavailable-slots")
                    || path.matches("/visits/vet/\\d+/unavailable-slots")
                    || path.startsWith("/visit/pet/")
                    || path.startsWith("/visits/pet/")
                    || path.startsWith("/payments/");
        }

        if (HttpMethod.PUT.equals(method)) {
            return path.startsWith("/customers/") || path.startsWith("/pets/");
        }

        if (HttpMethod.POST.equals(method)) {
            return path.startsWith("/pets/customer/")
                    || path.equals("/visit")
                    || path.equals("/visits")
                    || path.matches("/visit/cancel/\\d+")
                    || path.matches("/visits/cancel/\\d+")
                    || path.equals("/payments/initiate")
                    || path.equals("/payments/verify");
        }

        if (HttpMethod.DELETE.equals(method)) {
            return path.startsWith("/pets/") || path.startsWith("/visit/") || path.startsWith("/customers/");
        }

        return false;
    }

    private boolean isVetAccess(HttpMethod method, String path) {
        if (HttpMethod.GET.equals(method)) {
            return path.equals("/customers")
                    || path.startsWith("/vets/username/")
                    || path.startsWith("/vets/")
                    || path.startsWith("/visit/vet/")
                    || path.startsWith("/visits/vet/")
                    || path.startsWith("/visit/pet/");
        }

        if (HttpMethod.PUT.equals(method)) {
            return path.startsWith("/vets/");
        }

        if (HttpMethod.POST.equals(method)) {
            return path.matches("/vets/\\d+/leaves");
        }

        if (HttpMethod.PATCH.equals(method)) {
            return path.matches("/visit/\\d+/status") || path.matches("/visits/\\d+/status");
        }

        return false;
    }
}
