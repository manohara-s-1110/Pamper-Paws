package com.pamperpaw.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-for-api-gateway-1234567890";
    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    @Test
    void validateAndExtractRoleFromValidToken() {
        String token = Jwts.builder()
                .subject("manu")
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("CUSTOMER", jwtUtil.extractRole(token));
    }

    @Test
    void rejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("bad-token"));
    }
}
