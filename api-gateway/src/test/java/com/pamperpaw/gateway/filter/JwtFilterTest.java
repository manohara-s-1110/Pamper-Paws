package com.pamperpaw.gateway.filter;

import com.pamperpaw.gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtFilterTest {

    private static final String SECRET = "test-secret-key-for-api-gateway-1234567890";

    @Test
    void allowAuthEndpointsWithoutToken() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login").build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void allowRegisterEndpointWithoutToken() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/register").build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void rejectProtectedEndpointWithoutToken() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1").build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectProtectedEndpointWithInvalidToken() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1")
                        .header("Authorization", "Bearer invalid-token")
                        .build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void allowCustomerRoleToCustomerEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("CUSTOMER");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void allowAdminRoleToAdminEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("ADMIN");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void rejectCustomerRoleToAdminEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("CUSTOMER");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/admin")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void allowVetRoleToVetEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("VET");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/vets/1")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void rejectVetRoleToCustomerEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("VET");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void allowUnknownRoleToFallThrough() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET));
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("GUEST");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        GatewayFilterChain chain = e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    private String tokenForRole(String role) {
        return Jwts.builder()
                .subject("manu")
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }
}
