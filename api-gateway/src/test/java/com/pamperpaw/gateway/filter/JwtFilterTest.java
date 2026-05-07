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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1").build()
        );

        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void rejectProtectedEndpointWithInvalidToken() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
    void allowCustomerRoleToOwnProfileLookupEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("CUSTOMER");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/username/testuser")
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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
    void allowVetRoleToOwnProfileLookupEndpoint() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("VET");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/vets/username/testvet")
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
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
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
    void rejectUnknownRole() {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
        GatewayFilter filter = jwtFilter.apply(new JwtFilter.Config());
        String token = tokenForRole("GUEST");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        filter.filter(exchange, e -> Mono.empty()).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void optionsRequestAddsInternalServiceHeader() {
        MockServerWebExchange exchange = exchangeWithoutToken(MockServerHttpRequest.options("/payments/initiate"));

        applyFilter(exchange, e -> {
            e.getAttributes().put("passed", true);
            e.getAttributes().put("internalKey", e.getRequest().getHeaders().getFirst("X-Internal-Service-Key"));
            return Mono.empty();
        });

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
        assertEquals("internal-test-key", exchange.getAttribute("internalKey"));
    }

    @Test
    void swaggerDocsArePublic() {
        MockServerWebExchange exchange = exchangeWithoutToken(MockServerHttpRequest.get("/v3/api-docs/payments"));

        applyFilter(exchange, e -> {
            e.getAttributes().put("passed", true);
            return Mono.empty();
        });

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
    }

    @Test
    void customerRoleCanUseAllowedWriteAndPaymentEndpoints() {
        String token = tokenForRole("CUSTOMER");

        assertAllowed(MockServerHttpRequest.put("/customers/1").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.post("/visit/cancel/11").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.post("/payments/verify").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.delete("/pets/2").header("Authorization", "Bearer " + token));
    }

    @Test
    void vetRoleCanUseAllowedScheduleEndpoints() {
        String token = tokenForRole("VET");

        assertAllowed(MockServerHttpRequest.get("/visit/vet/7").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.put("/vets/7").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.post("/vets/7/leaves").header("Authorization", "Bearer " + token));
        assertAllowed(MockServerHttpRequest.patch("/visits/4/status").header("Authorization", "Bearer " + token));
    }

    @Test
    void customerRoleRejectsUnsupportedMethodPath() {
        String token = tokenForRole("CUSTOMER");
        MockServerWebExchange exchange = exchangeWithoutToken(
                MockServerHttpRequest.patch("/admin/1").header("Authorization", "Bearer " + token)
        );

        applyFilter(exchange, e -> Mono.empty());

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    private void assertAllowed(MockServerHttpRequest.BaseBuilder<?> requestBuilder) {
        MockServerWebExchange exchange = exchangeWithoutToken(requestBuilder);

        applyFilter(exchange, e -> {
            e.getAttributes().put("passed", true);
            e.getAttributes().put("username", e.getRequest().getHeaders().getFirst("X-User-Name"));
            e.getAttributes().put("role", e.getRequest().getHeaders().getFirst("X-User-Role"));
            return Mono.empty();
        });

        assertTrue(Boolean.TRUE.equals(exchange.getAttribute("passed")));
        assertEquals("manu", exchange.getAttribute("username"));
    }

    private MockServerWebExchange exchangeWithoutToken(MockServerHttpRequest.BaseBuilder<?> requestBuilder) {
        return MockServerWebExchange.from(requestBuilder.build());
    }

    private void applyFilter(MockServerWebExchange exchange, GatewayFilterChain chain) {
        JwtFilter jwtFilter = new JwtFilter(new JwtUtil(SECRET), "internal-test-key");
        jwtFilter.apply(new JwtFilter.Config()).filter(exchange, chain).block();
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
