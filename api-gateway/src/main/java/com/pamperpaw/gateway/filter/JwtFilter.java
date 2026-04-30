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
            if (!isAuthorized(role, method, path)) {
                log.warn("Forbidden role={} access to method={} path={}", role, method, path);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(withInternalServiceHeader(exchange.getRequest(), exchange));
        };
    }

    private ServerWebExchange withInternalServiceHeader(ServerHttpRequest request, ServerWebExchange exchange) {
        ServerHttpRequest requestWithInternalHeader = request
                .mutate()
                .header(INTERNAL_SERVICE_HEADER, internalServiceKey)
                .build();

        return exchange.mutate().request(requestWithInternalHeader).build();
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui/");
    }

    private boolean isAuthorized(String role, HttpMethod method, String path) {
        if ("ADMIN".equals(role)) {
            return path.startsWith("/admin")
                    || path.startsWith("/customers")
                    || path.startsWith("/pets")
                    || path.startsWith("/vets")
                    || path.startsWith("/visit");
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
                    || (path.startsWith("/visit/vet/") && (path.endsWith("/unavailable-slots") || path.endsWith("/leaves")))
                    || path.startsWith("/visit/pet/");
        }

        if (HttpMethod.PUT.equals(method)) {
            return path.startsWith("/customers/") || path.startsWith("/pets/");
        }

        if (HttpMethod.POST.equals(method)) {
            return path.startsWith("/pets/customer/") || path.equals("/visit");
        }

        if (HttpMethod.DELETE.equals(method)) {
            return path.startsWith("/pets/") || path.startsWith("/visit/");
        }

        return false;
    }

    private boolean isVetAccess(HttpMethod method, String path) {
        if (HttpMethod.GET.equals(method)) {
            return path.equals("/customers")
                    || path.startsWith("/vets/username/")
                    || path.startsWith("/visit/vet/")
                    || path.startsWith("/visit/pet/");
        }

        if (HttpMethod.PUT.equals(method)) {
            return path.startsWith("/vets/");
        }

        if (HttpMethod.POST.equals(method)) {
            return path.startsWith("/visit/vet/") && path.endsWith("/leaves");
        }

        if (HttpMethod.PATCH.equals(method)) {
            return path.startsWith("/visit/");
        }

        return false;
    }
}
